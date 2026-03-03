package com.ubik.paymentservice.domain.port.in;

import com.ubik.paymentservice.application.dto.CreatePaymentRequest;
import com.ubik.paymentservice.application.dto.PaymentResponse;
import com.ubik.paymentservice.application.dto.WebhookRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentUseCase {
    Mono<PaymentResponse> createPayment(CreatePaymentRequest request, Long userId);
    Mono<PaymentResponse> getPayment(Long paymentId);
    Flux<PaymentResponse> getPaymentsByReservation(Long reservationId);
    Flux<PaymentResponse> getPaymentsByUser(Long userId);
    Mono<Void>            processWebhook(WebhookRequest request);
    Mono<PaymentResponse> refundPayment(Long paymentId);
}
