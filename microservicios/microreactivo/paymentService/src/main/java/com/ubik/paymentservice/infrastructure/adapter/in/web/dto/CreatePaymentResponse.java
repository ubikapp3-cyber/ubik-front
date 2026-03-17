
package com.ubik.paymentservice.infrastructure.adapter.in.web.dto;

/**
 * Respuesta con el clientSecret que el frontend usa para confirmar el pago con Stripe.js
 */
public record CreatePaymentResponse(
        Long paymentId,
        String clientSecret
) {
}
