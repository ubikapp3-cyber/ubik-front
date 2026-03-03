package com.example.paymentservice.domain;

import java.time.LocalDateTime;

public record Payment(
        Long id,
        Long reservationId,
        Long userId,
        Double amount,
        String currency,
        PaymentStatus status,
        String mercadoPagoPaymentId,
        String mercadoPagoPreferenceId,
        String initPoint,           // URL de pago que se devuelve al cliente
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}