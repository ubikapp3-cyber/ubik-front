package com.example.paymentservice.adapters.out.persistence.mapper;

import com.acme.payments.adapters.out.persistence.entity.PaymentIntentEntity;
import com.acme.payments.domain.model.*;
import org.springframework.stereotype.Component;

@Component
public class PaymentIntentPersistenceMapper {

    public PaymentIntentEntity toEntity(PaymentIntent domain) {
        PaymentIntentEntity entity = new PaymentIntentEntity();
        entity.id = domain.getId();
        entity.externalReference = domain.getExternalReference();
        entity.amount = domain.getMoney().amount();
        entity.currency = domain.getMoney().currency();
        entity.status = domain.getStatus().name();
        entity.provider = domain.getProvider().name();
        entity.mpPreferenceId = domain.getMpPreferenceId();
        entity.mpPaymentId = domain.getMpPaymentId();
        entity.redirectUrl = domain.getRedirectUrl();
        entity.lastProviderStatus = domain.getLastProviderStatus();
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        entity.version = domain.getVersion();
        return entity;
    }

    public PaymentIntent toDomain(PaymentIntentEntity entity) {
        return new PaymentIntent(
                entity.id,
                entity.externalReference,
                Money.of(entity.amount, entity.currency),
                PaymentStatus.valueOf(entity.status),
                PaymentProvider.valueOf(entity.provider),
                entity.mpPreferenceId,
                entity.mpPaymentId,
                entity.redirectUrl,
                entity.lastProviderStatus,
                entity.createdAt,
                entity.updatedAt,
                entity.version
        );
    }
}