package com.ubik.paymentservice.application.dto;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long reservationId,
        Long userId,
        Long motelId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String initPoint,
        String mercadopagoPreferenceId,
        String mercadopagoPaymentId,
        String failureReason,
        BigDecimal marketplaceFee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.id(), p.reservationId(), p.userId(), p.motelId(),
                p.amount(), p.currency(), p.status(),
                p.initPoint(), p.mercadopagoPreferenceId(), p.mercadopagoPaymentId(),
                p.failureReason(), p.marketplaceFee(), p.createdAt(), p.updatedAt()
        );
    }
}
