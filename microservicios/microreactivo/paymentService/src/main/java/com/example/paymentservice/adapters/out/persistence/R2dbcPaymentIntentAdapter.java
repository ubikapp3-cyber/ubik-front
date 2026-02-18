package com.example.paymentservice.adapters.out.persistence;


import com.acme.payments.adapters.out.persistence.mapper.PaymentIntentPersistenceMapper;
import com.acme.payments.adapters.out.persistence.r2dbc.PaymentIntentR2dbcRepository;
import com.acme.payments.application.port.out.PaymentIntentRepositoryPort;
import com.acme.payments.domain.model.PaymentIntent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class R2dbcPaymentIntentAdapter implements PaymentIntentRepositoryPort {

    private final PaymentIntentR2dbcRepository repository;
    private final PaymentIntentPersistenceMapper mapper;

    public R2dbcPaymentIntentAdapter(PaymentIntentR2dbcRepository repository,
                                     PaymentIntentPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Void> save(PaymentIntent intent) {
        return repository.save(mapper.toEntity(intent)).then();
    }

    @Override
    public Mono<PaymentIntent> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Mono<PaymentIntent> findByExternalReference(String ref) {
        return repository.findByExternalReference(ref).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> update(PaymentIntent intent) {
        return repository.save(mapper.toEntity(intent)).then();
    }
}