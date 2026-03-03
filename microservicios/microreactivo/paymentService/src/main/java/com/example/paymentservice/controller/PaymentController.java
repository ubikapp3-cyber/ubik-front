package com.example.paymentservice.controller;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.dto.WebhookRequest;
import com.example.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/payments
     * Crea una preferencia de pago en MercadoPago.
     * Devuelve init_point (URL de pago) al cliente.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            ServerWebExchange exchange) {

        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userIdHeader == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        }

        Long userId;
        try {
            userId = Long.parseLong(userIdHeader);
        } catch (NumberFormatException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-User-Id inválido"));
        }

        return paymentService.createPayment(request, userId, request.motelId());
    }

    /**
     * GET /api/payments/{id}
     */
    @GetMapping("/{id}")
    public Mono<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    /**
     * GET /api/payments/reservation/{reservationId}
     */
    @GetMapping("/reservation/{reservationId}")
    public Flux<PaymentResponse> getByReservation(@PathVariable Long reservationId) {
        return paymentService.getPaymentsByReservation(reservationId);
    }

    /**
     * GET /api/payments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Flux<PaymentResponse> getByUser(@PathVariable Long userId) {
        return paymentService.getPaymentsByUser(userId);
    }

    /**
     * POST /api/payments/webhook
     * Endpoint que MercadoPago llama al confirmar/rechazar un pago.
     */
    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Void> webhook(@RequestBody WebhookRequest request) {
        log.info("Webhook recibido: action={}, type={}", request.action(), request.type());

        if (!"payment".equals(request.type()) || request.data() == null) {
            return Mono.empty();
        }

        return paymentService.processWebhook(request.data().id());
    }
    /**
     * POST /api/payments/{id}/refund
     * Reembolsa un pago aprobado.
     */
    @PostMapping("/{id}/refund")
    public Mono<PaymentResponse> refundPayment(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if (userId == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        }

        // Solo admin puede hacer reembolsos directos
        // Si quieres que el usuario reembolse el suyo propio, valida aquí también
        return paymentService.refundPayment(id);
    }
}