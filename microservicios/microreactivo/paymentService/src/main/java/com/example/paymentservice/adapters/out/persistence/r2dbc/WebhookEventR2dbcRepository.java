package com.example.paymentservice.adapters.out.persistence.r2dbc;

import com.acme.payments.adapters.out.persistence.entity.WebhookEventEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface WebhookEventR2dbcRepository extends ReactiveCrudRepository<WebhookEventEntity, UUID> {

    Mono<WebhookEventEntity> findByDedupKey(String dedupKey);

    @Modifying
    @Query("UPDATE webhook_event SET processed_at = :processedAt WHERE dedup_key = :dedupKey")
    Mono<Integer> markProcessedReturningCount(String dedupKey, Instant processedAt);
}