package com.example.paymentservice.application.port.out;


import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface IdempotencyStorePort {

    record IdemRecord(
            UUID id,
            String idemKey,
            String operation,
            UUID resourceId,
            String requestHash,
            Instant createdAt,
            Instant expiresAt
    ) {}

    Mono<IdemRecord> findValid(String key, String operation, Instant now);
    Mono<Void> save(IdemRecord record);
}
