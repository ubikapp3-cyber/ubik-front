package com.ubik.paymentservice.domain.port.out;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.model.PaymentStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistencia de pagos (R2DBC / PostgreSQL)
 */
public interface PaymentRepositoryPort {

    Mono<Payment> save(Payment payment);

    Mono<Payment> findById(Long id);

    Flux<Payment> findByReservationId(Long reservationId);

    Flux<Payment> findByUserId(Long userId);

    Mono<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Actualiza únicamente el status y failureMessage del pago.
     * Busca por stripePaymentIntentId para mapear el evento de Stripe.
     */
    Mono<Payment> updateStatus(String stripePaymentIntentId, PaymentStatus status, String failureMessage);
}
