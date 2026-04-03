package com.ubik.streakservice.infrastructure.adapter.out.reservation;

import com.ubik.streakservice.domain.port.out.ReservationQueryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Consulta directamente a motel-management-service por red interna Docker.
 * No pasa por el gateway (sin JWT requerido para llamadas internas).
 */
@Component
public class ReservationQueryAdapter implements ReservationQueryPort {

    private final WebClient webClient;

    public ReservationQueryAdapter(
            @Value("${services.motel-management.url:http://motel-management-service:8083}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Integer> countLast30Days(Long userId) {
        return webClient.get()
                .uri("/api/reservations/user/{userId}/count-last-30-days", userId)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(Integer.class)
                .onErrorReturn(0);   // si el servicio no responde → nivel NEW, no falla
    }
}
