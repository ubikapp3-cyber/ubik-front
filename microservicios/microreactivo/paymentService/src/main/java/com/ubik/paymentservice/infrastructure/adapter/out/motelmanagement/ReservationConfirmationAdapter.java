package com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement;

import com.ubik.paymentservice.domain.port.out.ReservationConfirmationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida que llama al endpoint de confirmación de reserva
 * en motel-management-service directamente sobre la red Docker (sin JWT).
 */
@Component
public class ReservationConfirmationAdapter implements ReservationConfirmationPort {

    private static final Logger log = LoggerFactory.getLogger(ReservationConfirmationAdapter.class);

    private final WebClient webClient;

    public ReservationConfirmationAdapter(
            @Value("${services.motel-management.url}") String motelManagementUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(motelManagementUrl)
                .build();
    }

    @Override
    public Mono<Void> confirmReservation(Long reservationId) {
        log.info("Confirmando reserva {} en motel-management-service", reservationId);

        return webClient.patch()
                .uri("/api/reservations/{id}/confirm", reservationId)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Reserva {} confirmada exitosamente", reservationId))
                .doOnError(e -> log.error("Error confirmando reserva {}: {}", reservationId, e.getMessage()));
    }

    @Override
    public Mono<com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement.dto.ReservationDto> getReservation(Long reservationId) {
        log.info("Obteniendo detalles de la reserva {} desde motel-management-service", reservationId);
        return webClient.get()
                .uri("/api/reservations/{id}", reservationId)
                .retrieve()
                .bodyToMono(com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement.dto.ReservationDto.class)
                .doOnError(e -> log.error("Error obteniendo detalles de reserva {}: {}", reservationId, e.getMessage()));
    }
}
