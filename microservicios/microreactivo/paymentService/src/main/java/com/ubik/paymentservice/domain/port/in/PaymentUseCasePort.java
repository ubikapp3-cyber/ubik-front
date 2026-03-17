package com.ubik.paymentservice.domain.port.in;


import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.infrastructure.adapter.in.web.dto.CreatePaymentResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (use case) para la gestión de pagos
 */
public interface PaymentUseCasePort {

    /**
     * Crea un PaymentIntent en Stripe y persiste el pago en estado PENDING.
     *
     * @param reservationId ID de la reserva a pagar
     * @param userId        ID del usuario que paga
     * @param amountCents   Monto en centavos (o la unidad mínima de la moneda)
     * @return clientSecret que el frontend usa para confirmar el pago con Stripe.js
     */
    Mono<CreatePaymentResponse> createPayment(Long reservationId, Long userId, Long amountCents);

    /**
     * Procesa un evento webhook enviado por Stripe.
     * Valida la firma y actualiza el estado del pago en la BD.
     * Si el pago es exitoso, confirma la reserva en motelManagement.
     */
    Mono<Void> handleWebhook(String payload, String stripeSignatureHeader);

    /**
     * Devuelve la publishable key de Stripe para que el frontend la use.
     */
    Mono<String> getPublishableKey();

    /**
     * Lista los pagos de un usuario.
     */
    Flux<Payment> findByUserId(Long userId);

    /**
     * Lista los pagos de una reserva.
     */
    Flux<Payment> findByReservationId(Long reservationId);
}
