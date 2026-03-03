package com.ubik.paymentservice.infrastructure.adapter.out.client;

import com.ubik.paymentservice.domain.port.out.ReservationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ReservationClient implements ReservationPort {

    private static final Logger log = LoggerFactory.getLogger(ReservationClient.class);
    private final WebClient webClient;

    public ReservationClient(
            @Value("${services.motel-management.url}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Void> confirmReservation(Long reservationId) {
        log.info("Confirmando reserva={}", reservationId);
        return webClient.patch()
                .uri("/api/reservations/{id}/confirm", reservationId)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Error confirmando reserva={}: {}", reservationId, e.getMessage()));
    }

    @Override
    public Mono<Void> cancelReservation(Long reservationId) {
        log.info("Cancelando reserva={}", reservationId);
        return webClient.patch()
                .uri("/api/reservations/{id}/cancel", reservationId)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Error cancelando reserva={}: {}", reservationId, e.getMessage()));
    }
}
