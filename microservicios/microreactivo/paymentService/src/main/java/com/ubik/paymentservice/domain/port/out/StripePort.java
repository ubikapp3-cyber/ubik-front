package com.ubik.paymentservice.domain.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida hacia la API de Stripe
 */
public interface StripePort {

    /**
     * Crea un PaymentIntent en Stripe y devuelve el clientSecret.
     * El clientSecret tiene formato "pi_xxx_secret_yyy"; de él se extrae el intentId.
     *
     * @param amountCents monto en la unidad mínima de la moneda (ej. centavos COP)
     * @param currency    código ISO de moneda en minúsculas (ej. "cop")
     * @param description descripción visible en el dashboard de Stripe
     * @return clientSecret del PaymentIntent
     */
    Mono<String> createPaymentIntent(Long amountCents, String currency, String description);

    /**
     * Devuelve la publishable key configurada (pk_test_... / pk_live_...).
     */
    Mono<String> getPublishableKey();

    /**
     * Valida la firma del webhook de Stripe.
     * Lanza excepción si la firma no es válida.
     *
     * @param payload     cuerpo raw del request (sin parsear)
     * @param sigHeader   valor del header "Stripe-Signature"
     */
    Mono<Void> validateWebhookSignature(String payload, String sigHeader);

    /**
     * Parsea el tipo de evento del JSON del webhook (ej. "payment_intent.succeeded").
     */
    Mono<String> parseEventType(String payload);

    /**
     * Extrae el stripePaymentIntentId ("pi_xxx") del JSON del evento webhook.
     */
    Mono<String> parsePaymentIntentId(String payload);

    /**
     * Extrae el mensaje de error del PaymentIntent fallido (puede ser null).
     */
    Mono<String> parseFailureMessage(String payload);
}
