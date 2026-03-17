package com.ubik.paymentservice.infrastructure.adapter.out.stripe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.ubik.paymentservice.domain.port.out.StripePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Adaptador de salida que implementa StripePort usando la SDK oficial de Stripe.
 * La SDK de Stripe es bloqueante, por eso usamos Schedulers.boundedElastic().
 *
 * Nota sobre parsing de eventos:
 * - Para validar la firma y obtener el Event se usa Webhook.constructEvent() directamente.
 * - Para obtener el tipo de evento desde el payload JSON crudo (sin validar firma) se usa Jackson,
 *   que ya está disponible via spring-boot-starter-webflux.
 */
@Component
public class StripeAdapter implements StripePort {

    private static final Logger log = LoggerFactory.getLogger(StripeAdapter.class);

    private final String publishableKey;
    private final String webhookSecret;
    private final ObjectMapper objectMapper;

    public StripeAdapter(
            @Value("${stripe.secret-key}") String secretKey,
            @Value("${stripe.publishable-key}") String publishableKey,
            @Value("${stripe.webhook-secret}") String webhookSecret,
            ObjectMapper objectMapper
    ) {
        this.publishableKey = publishableKey;
        this.webhookSecret = webhookSecret;
        this.objectMapper = objectMapper;
        Stripe.apiKey = secretKey;
        log.info("StripeAdapter inicializado");
    }

    @Override
    public Mono<String> createPaymentIntent(Long amountCents, String currency, String description) {
        return Mono.fromCallable(() -> {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency(currency)
                    .setDescription(description)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            log.info("PaymentIntent creado: {}", intent.getId());
            return intent.getClientSecret();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> getPublishableKey() {
        return Mono.just(publishableKey);
    }

    @Override
    public Mono<Void> validateWebhookSignature(String payload, String sigHeader) {
        return Mono.fromCallable(() -> {
            // constructEvent valida la firma Y parsea el evento
            Webhook.constructEvent(payload, sigHeader, webhookSecret);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<String> parseEventType(String payload) {
        // Usamos Jackson (disponible via WebFlux) para leer el campo "type" del JSON
        return Mono.fromCallable(() -> {
            var node = objectMapper.readTree(payload);
            return node.path("type").asText();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> parsePaymentIntentId(String payload) {
        // Construimos el Event con la firma vacía en modo "sin validar" para parsear el objeto.
        // La firma ya fue validada en validateWebhookSignature(); aquí solo necesitamos el ID.
        return Mono.fromCallable(() -> {
            var node = objectMapper.readTree(payload);
            // El ID del PaymentIntent está en data.object.id para eventos payment_intent.*
            return node.path("data").path("object").path("id").asText();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<String> parseFailureMessage(String payload) {
        return Mono.fromCallable(() -> {
            var node = objectMapper.readTree(payload);
            // Para payment_intent.payment_failed, el mensaje de error está en
            // data.object.last_payment_error.message
            String message = node
                    .path("data")
                    .path("object")
                    .path("last_payment_error")
                    .path("message")
                    .asText(null);
            return message != null ? message : "Error de pago";
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
