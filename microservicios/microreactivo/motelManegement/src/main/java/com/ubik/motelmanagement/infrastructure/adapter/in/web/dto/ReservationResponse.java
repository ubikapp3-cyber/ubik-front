package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Reservation
 */
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