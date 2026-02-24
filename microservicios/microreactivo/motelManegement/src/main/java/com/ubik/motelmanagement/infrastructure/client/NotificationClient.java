package com.ubik.motelmanagement.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Cliente HTTP reactivo para comunicación con NotificationService
 */
@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final WebClient webClient;

    public NotificationClient(@Value("${notification.service.url:http://notificationService:8084}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Envía email de confirmación de forma asíncrona (no bloquea la respuesta)
     *
     * @param to Email del destinatario
     * @param subject Asunto
     * @param message Cuerpo del mensaje
     * @return Mono<Void> que se ejecuta en background
     */
    public Mono<Void> sendEmailAsync(String to, String subject, String message) {
        log.info("📧 Enviando email asíncrono a: {}", to);

        return webClient.post()
                .uri("/notifications/email")
                .header("X-Internal-Request", "true")
                .bodyValue(new EmailRequest(to, subject, message))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribeOn(Schedulers.boundedElastic())  // Ejecución asíncrona
                .doOnSuccess(v -> log.info("Email enviado exitosamente a: {}", to))
                .doOnError(error -> log.error("Error enviando email a {}: {}", to, error.getMessage()))
                .onErrorResume(error -> Mono.empty());  // No fallar si el email falla
    }

    /**
     * DTO interno para el request
     */
    private record EmailRequest(String to, String subject, String message) {}
}