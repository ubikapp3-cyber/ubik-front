package com.example.paymentservice.adapters.out.persistence.r2dbc;

import com.acme.payments.adapters.out.persistence.entity.IdempotencyKeyEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface IdempotencyKeyR2dbcRepository extends ReactiveCrudRepository<IdempotencyKeyEntity, UUID> {

    @Query("SELECT * FROM idempotency_key WHERE iem_key = :key AND operation = :operation AND expires_at > :now LIMIT 1")
    Mono<IdempotencyKeyEntity> findValidRecord(String key, String operation, Instant now);
}
