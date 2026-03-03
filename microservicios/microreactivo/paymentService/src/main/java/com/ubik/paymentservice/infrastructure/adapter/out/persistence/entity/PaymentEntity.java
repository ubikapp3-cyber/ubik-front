package com.ubik.paymentservice.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("payments")
public record PaymentEntity(
        @Id                                  Long id,
        @Column("reservation_id")            Long reservationId,
        @Column("user_id")                   Long userId,
        @Column("motel_id")                  Long motelId,
        @Column("amount")                    BigDecimal amount,
        @Column("currency")                  String currency,
        @Column("status")                    String status,
        @Column("mercadopago_payment_id")    String mercadopagoPaymentId,
        @Column("mercadopago_preference_id") String mercadopagoPreferenceId,
        @Column("init_point")                String initPoint,
        @Column("failure_reason")            String failureReason,
        @Column("marketplace_fee")           BigDecimal marketplaceFee,
        @Column("created_at")                LocalDateTime createdAt,
        @Column("updated_at")                LocalDateTime updatedAt
) {}
