package com.example.paymentservice.adapters.out.persistence.r2dbc;

import com.acme.payments.adapters.out.persistence.entity.PaymentIntentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentIntentR2dbcRepository extends ReactiveCrudRepository<PaymentIntentEntity, UUID> {
    Mono<PaymentIntentEntity> findByExternalReference(String externalReference);
}