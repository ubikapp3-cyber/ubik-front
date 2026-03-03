package com.example.paymentservice.dto;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long roomId,
        Long userId,
        LocalDateTime checkInDate,
        LocalDateTime checkOutDate,
        String status,
        Double totalPrice,
        String specialRequests,
        String confirmationCode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}