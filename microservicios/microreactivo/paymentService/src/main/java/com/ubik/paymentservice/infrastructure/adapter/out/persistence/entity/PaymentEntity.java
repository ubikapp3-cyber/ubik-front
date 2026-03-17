package com.ubik.paymentservice.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para la tabla "payments" (R2DBC / PostgreSQL).
 * Sigue el mismo patrón que ReservationEntity en motelManagement.
 */
@Table("payments")
public record PaymentEntity(

        @Id
        Long id,

        @Column("reservation_id")
        Long reservationId,

        @Column("user_id")
        Long userId,

        @Column("motel_id")
        Long motelId,

        @Column("amount") // legado de MercadoPago, requerido NOT NULL
        Long amount,

        @Column("stripe_payment_intent_id")
        String stripePaymentIntentId,

        @Column("amount_cents")
        Long amountCents,

        String currency,

        String status,         // almacenado como String (nombre del enum)

        @Column("failure_message")
        String failureMessage,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("updated_at")
        LocalDateTime updatedAt
) {
}
