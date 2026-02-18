package com.example.paymentservice.adapters.out.persistence;


import com.acme.payments.adapters.out.persistence.entity.WebhookEventEntity;
import com.acme.payments.adapters.out.persistence.r2dbc.WebhookEventR2dbcRepository;
import com.acme.payments.application.port.out.WebhookEventStorePort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class R2dbcWebhookEventAdapter implements WebhookEventStorePort {

    private final WebhookEventR2dbcRepository repository;

    public R2dbcWebhookEventAdapter(WebhookEventR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<WebhookEventRecord> findByDedupKey(String dedupKey) {
        return repository.findByDedupKey(dedupKey).map(this::toDomain);
    }

    @Override
    public Mono<Void> save(WebhookEventRecord record) {
        WebhookEventEntity entity = new WebhookEventEntity();
        entity.id = record.id();
        entity.dedupKey = record.dedupKey();
        entity.topic = record.topic();
        entity.action = record.action();
        entity.dataId = record.dataId();
        entity.xRequestId = record.xRequestId();
        entity.signatureValid = record.signatureValid();
        entity.receivedAt = record.receivedAt();
        entity.processedAt = record.processedAt();
        entity.payloadJson = record.payloadJson();
        entity.headersJson = record.headersJson();
        return repository.save(entity).then();
    }

    @Override
    public Mono<Void> markProcessed(String dedupKey, Instant processedAt) {
        return repository.markProcessedReturningCount(dedupKey, processedAt).then();
    }

    private WebhookEventRecord toDomain(WebhookEventEntity e) {
        return new WebhookEventRecord(e.id, e.dedupKey, e.topic, e.action, e.dataId,
                e.xRequestId, e.signatureValid, e.receivedAt, e.processedAt,
                e.payloadJson, e.headersJson);
    }
}
