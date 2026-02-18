package com.example.paymentservice.adapters.out.persistence.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("idempotency_key")
public class IdempotencyKeyEntity {

    @Id
    public UUID id;
    public String idemKey;
    public String operation;
    public UUID resourceId;
    public String requestHash;
    public Instant createdAt;
    public Instant expiresAt;
}