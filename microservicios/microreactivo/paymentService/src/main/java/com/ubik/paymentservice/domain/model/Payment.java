package com.ubik.paymentservice.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para Payment (inmutable record)
 * Representa un pago procesado a través de Stripe
 */
public record Payment(
        Long id,
        Long reservationId,
        Long userId,
        String stripePaymentIntentId,  // "pi_xxx" extraído del clientSecret
        Long amountCents,              // Stripe siempre trabaja en las unidades menores de la moneda
        String currency,               // "cop"
        PaymentStatus status,
        String failureMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Factory method para crear un nuevo pago pendiente
     */
    public static Payment createPending(
            Long reservationId,
            Long userId,
            String stripePaymentIntentId,
            Long amountCents,
            String currency
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Payment(
                null,
                reservationId,
                userId,
                stripePaymentIntentId,
                amountCents,
                currency,
                PaymentStatus.PENDING,
                null,
                now,
                now
        );
    }

    /**
     * Devuelve una copia del pago con el estado actualizado
     */
    public Payment withStatus(PaymentStatus newStatus) {
        return new Payment(
                this.id,
                this.reservationId,
                this.userId,
                this.stripePaymentIntentId,
                this.amountCents,
                this.currency,
                newStatus,
                this.failureMessage,
                this.createdAt,
                LocalDateTime.now()
        );
    }

    /**
     * Devuelve una copia con estado fallido y mensaje de error
     */
    public Payment withFailure(String message) {
        return new Payment(
                this.id,
                this.reservationId,
                this.userId,
                this.stripePaymentIntentId,
                this.amountCents,
                this.currency,
                PaymentStatus.FAILED,
                message,
                this.createdAt,
                LocalDateTime.now()
        );
    }
}
