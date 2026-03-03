package com.example.paymentservice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("payments")
public record PaymentEntity(
        @Id Long id,
        @Column("reservation_id") Long reservationId,
        @Column("user_id") Long userId,
        Double amount,
        String currency,
        String status,
        @Column("mercadopago_payment_id") String mercadoPagoPaymentId,
        @Column("mercadopago_preference_id") String mercadoPagoPreferenceId,
        @Column("init_point") String initPoint,
        @Column("failure_reason") String failureReason,
        @Column("marketplace_fee") Double marketplaceFee,
        @Column("motel_id") Long motelId,
        @Column("mp_collector_id") String mpCollectorId,
        @Column("created_at") LocalDateTime createdAt,
        @Column("updated_at") LocalDateTime updatedAt
) {}