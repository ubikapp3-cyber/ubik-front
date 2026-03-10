package com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class ReservationInfoAdapter {

    private static final Logger log = LoggerFactory.getLogger(ReservationInfoAdapter.class);
    private final WebClient webClient;

    public ReservationInfoAdapter(@Value("${services.motel-management.url:http://motel-management-service:8083}") String motelManagementUrl) {
         this.webClient = WebClient.builder()
                 .baseUrl(motelManagementUrl)
                 .build();
    }

    public Mono<ReservationDto> getReservationInfo(Long reservationId) {
        log.info("Obteniendo información de la reserva {}", reservationId);

        return webClient.get()
                .uri("/api/reservations/{id}", reservationId)
                .retrieve()
                .bodyToMono(ReservationDto.class)
                .doOnError(e -> log.error("Error obteniendo reserva {}: {}", reservationId, e.getMessage()));
    }

    public record ReservationDto(Long id, Long roomId, LocalDateTime checkInDate, LocalDateTime checkOutDate, Double totalPrice) {}
}
