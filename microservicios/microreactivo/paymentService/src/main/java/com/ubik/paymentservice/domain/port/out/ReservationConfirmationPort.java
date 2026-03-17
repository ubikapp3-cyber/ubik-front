package com.ubik.paymentservice.domain.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida hacia el microservicio motelManagement.
 * Permite confirmar una reserva cuando el pago es exitoso.
 */
public interface ReservationConfirmationPort {

    /**
     * Confirma una reserva en motelManagement via PATCH /api/reservations/{id}/confirm.
     * La llamada es directa sobre la red Docker (sin pasar por el gateway) por lo que
     * no requiere JWT.
     *
     * @param reservationId ID de la reserva a confirmar
     * @return Mono vacío cuando la confirmación es aceptada
     */
    Mono<Void> confirmReservation(Long reservationId);

    /**
     * Obtiene los detalles de la reserva.
     */
    Mono<com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement.dto.ReservationDto> getReservation(Long reservationId);
}
