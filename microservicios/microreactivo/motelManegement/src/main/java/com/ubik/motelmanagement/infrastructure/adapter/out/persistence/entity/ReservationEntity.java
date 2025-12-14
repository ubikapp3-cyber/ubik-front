package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para Reservation (reservas)
 */
@Table("reservations")
public record ReservationEntity(
        @Id Long id,
        Long roomId,
        Long userId,
        LocalDateTime checkInDate,
        LocalDateTime checkOutDate,
        String status,
        Double totalPrice,
        String specialRequests,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
