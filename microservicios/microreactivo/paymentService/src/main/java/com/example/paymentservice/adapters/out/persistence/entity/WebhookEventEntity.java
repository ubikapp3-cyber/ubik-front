package com.example.paymentservice.adapters.out.persistence.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("webhook_event")
public class WebhookEventEntity {

    @Id
    public UUID id;
    public String dedupKey;
    public String topic;
    public String action;
    public String dataId;
    public String xRequestId;
    public boolean signatureValid;
    public Instant receivedAt;
    public Instant processedAt;
    public String payloadJson;
    public String headersJson;
}