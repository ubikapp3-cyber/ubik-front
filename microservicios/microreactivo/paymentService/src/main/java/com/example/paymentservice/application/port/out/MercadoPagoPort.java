package com.example.paymentservice.application.port.out;


import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface MercadoPagoPort {

    record CreatePreferenceRequest(
            String externalReference,
            String title,
            BigDecimal unitPrice,
            String currency,
            String notificationUrl,
            String successUrl,
            String pendingUrl,
            String failureUrl
    ) {}

    record CreatedPreference(
            String preferenceId,
            String initPoint,
            String sandboxInitPoint
    ) {}

    record PaymentSnapshot(
            Long paymentId,
            String status,
            String statusDetail,
            BigDecimal transactionAmount,
            String currency
    ) {}

    record RefundSnapshot(
            Long refundId,
            BigDecimal amount,
            String status
    ) {}

    Mono<CreatedPreference> createPreference(CreatePreferenceRequest req, String idempotencyKey);
    Mono<PaymentSnapshot> getPayment(Long paymentId);
    Mono<RefundSnapshot> refundPayment(Long paymentId, BigDecimal amount, String idempotencyKey);
}
