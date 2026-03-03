package com.example.paymentservice.service;

import com.example.paymentservice.entity.MotelMpAccountEntity;
import com.example.paymentservice.repository.MotelMpAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OAuthService {

    private static final Logger log = LoggerFactory.getLogger(OAuthService.class);

    private final MotelMpAccountRepository mpAccountRepository;
    private final WebClient webClient;

    @Value("${mercadopago.client-id}")
    private String clientId;

    @Value("${mercadopago.client-secret}")
    private String clientSecret;

    @Value("${mercadopago.redirect-uri}")
    private String redirectUri;

    public OAuthService(
            MotelMpAccountRepository mpAccountRepository,
            WebClient.Builder builder) {
        this.mpAccountRepository = mpAccountRepository;
        this.webClient = builder.baseUrl("https://api.mercadopago.com").build();
    }

    /**
     * Genera la URL a la que debes redirigir al dueño del motel
     * para que autorice tu app en su cuenta de MercadoPago
     */
    public String getAuthorizationUrl(Long motelId) {
        return "https://auth.mercadopago.com/authorization" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&platform_id=mp" +
                "&state=" + motelId +   // state = motelId para identificarlo al volver
                "&redirect_uri=" + redirectUri;
    }

    /**
     * MercadoPago redirige aquí con ?code=XXX&state=motelId
     * Intercambiamos el code por access_token y lo guardamos
     */
    public Mono<MotelMpAccountEntity> handleOAuthCallback(String code, Long motelId) {
        log.info("Procesando OAuth callback para motelId: {}", motelId);

        return webClient.post()
                .uri("/oauth/token")
                .bodyValue(Map.of(
                        "client_secret", clientSecret,
                        "code", code,
                        "grant_type", "authorization_code",
                        "redirect_uri", redirectUri
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    String accessToken  = (String) response.get("access_token");
                    String refreshToken = (String) response.get("refresh_token");
                    String mpUserId     = response.get("user_id").toString();
                    Integer expiresIn   = ((Number) response.get("expires_in")).intValue();

                    return mpAccountRepository.findByMotelId(motelId)
                            .defaultIfEmpty(new MotelMpAccountEntity(
                                    null, motelId, mpUserId, accessToken, refreshToken,
                                    LocalDateTime.now().plusSeconds(expiresIn),
                                    null, LocalDateTime.now(), LocalDateTime.now()
                            ))
                            .flatMap(existing -> {
                                // Actualizar o crear
                                MotelMpAccountEntity updated = new MotelMpAccountEntity(
                                        existing.id(),
                                        motelId,
                                        mpUserId,
                                        accessToken,
                                        refreshToken,
                                        LocalDateTime.now().plusSeconds(expiresIn),
                                        existing.mpEmail(),
                                        existing.createdAt() != null ? existing.createdAt() : LocalDateTime.now(),
                                        LocalDateTime.now()
                                );
                                return mpAccountRepository.save(updated);
                            });
                })
                .doOnSuccess(account -> log.info("Cuenta MP vinculada para motelId: {}", motelId))
                .doOnError(e -> log.error("Error en OAuth callback: {}", e.getMessage()));
    }

    /**
     * Refresca el access_token cuando está por vencer
     */
    public Mono<MotelMpAccountEntity> refreshToken(Long motelId) {
        return mpAccountRepository.findByMotelId(motelId)
                .switchIfEmpty(Mono.error(new RuntimeException("Cuenta MP no encontrada para motel: " + motelId)))
                .flatMap(account -> webClient.post()
                        .uri("/oauth/token")
                        .bodyValue(Map.of(
                                "client_secret", clientSecret,
                                "grant_type", "refresh_token",
                                "refresh_token", account.refreshToken()
                        ))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .flatMap(response -> {
                            MotelMpAccountEntity updated = new MotelMpAccountEntity(
                                    account.id(),
                                    account.motelId(),
                                    account.mpUserId(),
                                    (String) response.get("access_token"),
                                    (String) response.get("refresh_token"),
                                    LocalDateTime.now().plusSeconds(((Number) response.get("expires_in")).longValue()),
                                    account.mpEmail(),
                                    account.createdAt(),
                                    LocalDateTime.now()
                            );
                            return mpAccountRepository.save(updated);
                        })
                );
    }

    /**
     * Obtiene el access_token vigente del motel,
     * refrescándolo automáticamente si está por vencer
     */
    public Mono<String> getValidAccessToken(Long motelId) {
        return mpAccountRepository.findByMotelId(motelId)
                .switchIfEmpty(Mono.error(new RuntimeException(
                        "El motel " + motelId + " no tiene cuenta MercadoPago vinculada")))
                .flatMap(account -> {
                    boolean isExpiringSoon = account.tokenExpiresAt() != null &&
                            account.tokenExpiresAt().isBefore(LocalDateTime.now().plusHours(1));

                    if (isExpiringSoon) {
                        return refreshToken(motelId).map(MotelMpAccountEntity::accessToken);
                    }
                    return Mono.just(account.accessToken());
                });
    }

    /**
     * Elimina la vinculación de la cuenta de MercadoPago para un motel
     */
    public Mono<Void> disconnect(Long motelId) {
        log.info("Desvinculando cuenta MP para motelId: {}", motelId);
        return mpAccountRepository.findByMotelId(motelId)
                .flatMap(mpAccountRepository::delete);
    }
}