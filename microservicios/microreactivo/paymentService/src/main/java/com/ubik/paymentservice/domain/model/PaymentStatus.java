package com.ubik.paymentservice.domain.model;

/**
 * Estados posibles de un pago con Stripe
 */
public enum PaymentStatus {
    PENDING,    // PaymentIntent creado, esperando confirmación
    SUCCEEDED,  // Pago exitoso (confirmado por webhook)
    FAILED,     // Pago fallido (confirmado por webhook)
    CANCELED    // Cancelado
}
