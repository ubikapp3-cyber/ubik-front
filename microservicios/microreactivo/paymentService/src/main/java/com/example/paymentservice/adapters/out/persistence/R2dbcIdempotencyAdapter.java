package com.example.paymentservice.adapters.out.persistence;


import com.acme.payments.adapters.out.persistence.entity.IdempotencyKeyEntity;
import com.acme.payments.adapters.out.persistence.r2dbc.IdempotencyKeyR2dbcRepository;
import com.acme.payments.application.port.out.IdempotencyStorePort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class R2dbcIdempotencyAdapter implements IdempotencyStorePort {

    private final IdempotencyKeyR2dbcRepository repository;

    public R2dbcIdempotencyAdapter(IdempotencyKeyR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<IdemRecord> findValid(String key, String operation, Instant now) {
        return repository.findValidRecord(key, operation, now)
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> save(IdemRecord record) {
        IdempotencyKeyEntity entity = new IdempotencyKeyEntity();
        entity.id = record.id();
        entity.idemKey = record.idemKey();
        entity.operation = record.operation();
        entity.resourceId = record.resourceId();
        entity.requestHash = record.requestHash();
        entity.createdAt = record.createdAt();
        entity.expiresAt = record.expiresAt();
        return repository.save(entity).then();
    }

    private IdemRecord toDomain(IdempotencyKeyEntity e) {
        return new IdemRecord(e.id, e.idemKey, e.operation, e.resourceId,
                e.requestHash, e.createdAt, e.expiresAt);
    }
}