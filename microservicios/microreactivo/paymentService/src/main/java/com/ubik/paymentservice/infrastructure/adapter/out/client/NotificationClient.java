package com.ubik.paymentservice.infrastructure.adapter.out.client;

import com.ubik.paymentservice.domain.port.out.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class NotificationClient implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);
    private final WebClient webClient;

    public NotificationClient(
            @Value("${services.notification.url}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Void> sendPaymentApproved(Long userId, Long reservationId, BigDecimal amount) {
        return send(Map.of(
                "userId",        userId,
                "type",          "PAYMENT_APPROVED",
                "reservationId", reservationId,
                "amount",        amount
        ));
    }

    @Override
    public Mono<Void> sendPaymentRejected(Long userId, Long reservationId, String reason) {
        return send(Map.of(
                "userId",        userId,
                "type",          "PAYMENT_REJECTED",
                "reservationId", reservationId,
                "reason",        reason
        ));
    }

    private Mono<Void> send(Map<String, Object> body) {
        return webClient.post()
                .uri("/notifications/email")
                .header("X-Internal-Request", "true")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.error("Error enviando notificación: {}", e.getMessage());
                    return Mono.empty();
                });
    }
}
