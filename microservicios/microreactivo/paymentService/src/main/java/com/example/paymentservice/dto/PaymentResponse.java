package com.example.paymentservice.dto;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long reservationId,
        Long userId,
        Double amount,
        String currency,
        String status,
        String mercadoPagoPaymentId,
        String mercadoPagoPreferenceId,
        String initPoint,       // URL para redirigir al usuario a pagar
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}