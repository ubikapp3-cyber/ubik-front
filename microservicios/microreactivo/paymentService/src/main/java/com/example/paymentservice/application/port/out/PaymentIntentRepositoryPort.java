package com.example.paymentservice.application.port.out;


import com.acme.payments.domain.model.PaymentIntent;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentIntentRepositoryPort {
    Mono<Void> save(PaymentIntent intent);
    Mono<PaymentIntent> findById(UUID id);
    Mono<PaymentIntent> findByExternalReference(String ref);
    Mono<Void> update(PaymentIntent intent);
}
