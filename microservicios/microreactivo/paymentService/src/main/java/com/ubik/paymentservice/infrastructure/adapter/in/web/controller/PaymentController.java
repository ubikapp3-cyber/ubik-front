package com.ubik.paymentservice.infrastructure.adapter.in.web.controller;

import com.ubik.paymentservice.application.dto.CreatePaymentRequest;
import com.ubik.paymentservice.application.dto.PaymentResponse;
import com.ubik.paymentservice.application.dto.WebhookRequest;
import com.ubik.paymentservice.domain.port.in.PaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    public PaymentController(PaymentUseCase paymentUseCase) {
        this.paymentUseCase = paymentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta X-User-Id"));
        }
        return paymentUseCase.createPayment(request, userId);
    }

    @PostMapping("/webhook")
    public Mono<Void> webhook(@RequestBody WebhookRequest request) {
        return paymentUseCase.processWebhook(request);
    }

    @GetMapping("/{id}")
    public Mono<PaymentResponse> getById(@PathVariable Long id) {
        return paymentUseCase.getPayment(id);
    }

    @GetMapping("/reservation/{reservationId}")
    public Flux<PaymentResponse> getByReservation(@PathVariable Long reservationId) {
        return paymentUseCase.getPaymentsByReservation(reservationId);
    }

    @GetMapping("/user/{userId}")
    public Flux<PaymentResponse> getByUser(@PathVariable Long userId) {
        return paymentUseCase.getPaymentsByUser(userId);
    }

    @PostMapping("/{id}/refund")
    public Mono<PaymentResponse> refund(@PathVariable Long id) {
        return paymentUseCase.refundPayment(id);
    }
}
