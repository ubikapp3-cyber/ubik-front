package com.ubik.paymentservice.domain.port.out;

import reactor.core.publisher.Mono;

public interface ReservationPort {
    Mono<Void> confirmReservation(Long reservationId);
    Mono<Void> cancelReservation(Long reservationId);
}
