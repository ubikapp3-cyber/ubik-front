package com.example.paymentservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MotelManagementClient {

    private static final Logger log = LoggerFactory.getLogger(MotelManagementClient.class);
    private final WebClient webClient;

    public MotelManagementClient(
            @Value("${services.motel-management.url}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<Void> confirmReservation(Long reservationId) {
        log.info("Confirmando reserva {} en motelManagement", reservationId);
        return webClient.patch()
                .uri("/api/reservations/{id}/confirm", reservationId)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.error("Error confirmando reserva {}: {}", reservationId, e.getMessage());
                    return Mono.empty();
                });
    }

    public Mono<Void> cancelReservation(Long reservationId) {
        log.info("Cancelando reserva {} en motelManagement", reservationId);
        return webClient.patch()
                .uri("/api/reservations/{id}/cancel", reservationId)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> {
                    log.error("Error cancelando reserva {}: {}", reservationId, e.getMessage());
                    return Mono.empty();
                });
    }
}