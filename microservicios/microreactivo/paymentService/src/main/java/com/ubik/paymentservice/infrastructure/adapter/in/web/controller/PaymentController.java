package com.ubik.paymentservice.infrastructure.adapter.in.web.controller;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.port.in.PaymentUseCasePort;
import com.ubik.paymentservice.infrastructure.adapter.in.web.dto.CreatePaymentRequest;
import com.ubik.paymentservice.infrastructure.adapter.in.web.dto.CreatePaymentResponse;
import com.ubik.paymentservice.infrastructure.adapter.in.web.dto.PaymentResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controlador REST para la gestión de pagos con Stripe.
 * Las rutas /api/payments/** ya están configuradas en el gateway.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentUseCasePort paymentUseCasePort;

    public PaymentController(PaymentUseCasePort paymentUseCasePort) {
        this.paymentUseCasePort = paymentUseCasePort;
    }

    /**
     * Crea un PaymentIntent en Stripe y devuelve el clientSecret al frontend.
     * POST /api/payments/create-intent
     * Requiere: JWT (el gateway extrae X-User-Id del token)
     */
    @PostMapping("/create-intent")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CreatePaymentResponse> createPaymentIntent(
            @Valid @RequestBody CreatePaymentRequest request,
            ServerWebExchange exchange
    ) {
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de usuario inválido"));
        }

        log.info("POST /api/payments/create-intent - reserva: {}, usuario: {}, monto: {} cop (enviando {} a Stripe)",
                request.reservationId(), userId, request.amountCop(), request.amountCop() * 100L);

        Long amountCents = request.amountCop() * 100L;
        return paymentUseCasePort.createPayment(request.reservationId(), userId, amountCents);
    }

    /**
     * Devuelve la publishable key de Stripe al frontend.
     * GET /api/payments/config
     */
    @GetMapping("/config")
    public Mono<Map<String, String>> getConfig() {
        return paymentUseCasePort.getPublishableKey()
                .map(key -> Map.of("publishableKey", key));
    }

    /**
     * Recibe eventos webhook de Stripe (sin JWT — ya permitido en SecurityConfig).
     * POST /api/payments/webhook
     */
    @PostMapping("/webhook")
    public Mono<ResponseEntity<Void>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        log.info("POST /api/payments/webhook - procesando evento Stripe");

        return paymentUseCasePort.handleWebhook(payload, sigHeader)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorResume(e -> {
                    log.error("Error procesando webhook: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().<Void>build());
                });
    }

    /**
     * Lista los pagos del usuario autenticado.
     * GET /api/payments/my-payments
     */
    @GetMapping("/my-payments")
    public Flux<PaymentResponse> getMyPayments(ServerWebExchange exchange) {
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if (userIdHeader == null || userIdHeader.isBlank()) {
            return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        }

        Long userId = Long.parseLong(userIdHeader);
        return paymentUseCasePort.findByUserId(userId)
                .map(this::toResponse);
    }

    /**
     * Lista los pagos de una reserva específica.
     * GET /api/payments/reservation/{reservationId}
     */
    @GetMapping("/reservation/{reservationId}")
    public Flux<PaymentResponse> getPaymentsByReservation(@PathVariable Long reservationId) {
        return paymentUseCasePort.findByReservationId(reservationId)
                .map(this::toResponse);
    }

    // ─── Mapper privado ────────────────────────────────────────────────────────

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.id(),
                payment.reservationId(),
                payment.userId(),
                payment.stripePaymentIntentId(),
                payment.amountCents(),
                payment.currency(),
                payment.status(),
                payment.failureMessage(),
                payment.createdAt(),
                payment.updatedAt()
        );
    }
}
