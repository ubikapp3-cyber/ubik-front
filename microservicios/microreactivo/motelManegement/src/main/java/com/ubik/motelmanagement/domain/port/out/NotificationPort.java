package com.ubik.motelmanagement.domain.port.out;

import reactor.core.publisher.Mono;

public interface NotificationPort {
    Mono<Void> sendReservationConfirmation(
            String email,
            String confirmationCode,
            String checkIn,
            String checkOut,
            String roomId,
            String totalPrice
    );
}
