package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para Reservation (reservas)
 * FIX: Agregadas anotaciones @Column para mapeo correcto camelCase → snake_case
 */
@Table("reservations")
public record ReservationEntity(
        @Id Long id,

        @Column("room_id")
        Long roomId,

        @Column("user_id")
        Long userId,

        @Column("check_in_date")
        LocalDateTime checkInDate,

        @Column("check_out_date")
        LocalDateTime checkOutDate,

        String status,

        @Column("total_price")
        Double totalPrice,

        @Column("special_requests")
        String specialRequests,

        @Column("confirmation_code")
        String confirmationCode,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("updated_at")
        LocalDateTime updatedAt
) {
}