package com.ubik.paymentservice.infrastructure.adapter.out.motelmanagement.dto;

import java.time.LocalDateTime;

public record ReservationDto(
        Long id,
        Long roomId,
        Long userId,
        LocalDateTime checkInDate,
        LocalDateTime checkOutDate,
        Double totalPrice
) {}
