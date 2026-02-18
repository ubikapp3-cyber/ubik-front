package com.example.paymentservice.application.port.out;


import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface WebhookEventStorePort {

    record WebhookEventRecord(
            UUID id,
            String dedupKey,
            String topic,
            String action,
            String dataId,
            String xRequestId,
            boolean signatureValid,
            Instant receivedAt,
            Instant processedAt,
            String payloadJson,
            String headersJson
    ) {}

    Mono<WebhookEventRecord> findByDedupKey(String dedupKey);
    Mono<Void> save(WebhookEventRecord record);
    Mono<Void> markProcessed(String dedupKey, Instant processedAt);
}
