package com.example.paymentservice.adapters.out.persistence.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("payment_intent")
public class PaymentIntentEntity {

    @Id
    public UUID id;
    public String externalReference;
    public BigDecimal amount;
    public String currency;
    public String status;
    public String provider;
    public String mpPreferenceId;
    public Long mpPaymentId;
    public String redirectUrl;
    public String lastProviderStatus;
    public Instant createdAt;
    public Instant updatedAt;
    @Version
    public Long version;
}