package com.ubik.paymentservice.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request para crear un PaymentIntent en Stripe
 */
public record CreatePaymentRequest(
        @NotNull(message = "El ID de la reserva es requerido")
        Long reservationId,

        @NotNull(message = "El monto es requerido")
        @Min(value = 1, message = "El monto debe ser mayor a cero")
        Long amountCop
) {
}
