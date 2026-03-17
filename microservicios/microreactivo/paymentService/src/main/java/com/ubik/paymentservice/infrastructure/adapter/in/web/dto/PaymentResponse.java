package com.ubik.paymentservice.infrastructure.adapter.in.web.dto;

import com.ubik.paymentservice.domain.model.PaymentStatus;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para exponer un pago al cliente
 */
public record PaymentResponse(
        Long id,
        Long reservationId,
        Long userId,
        String stripePaymentIntentId,
        Long amountCents,
        String currency,
        PaymentStatus status,
        String failureMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
