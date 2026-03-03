package com.ubik.paymentservice.infrastructure.adapter.out.persistence.mapper;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.model.PaymentStatus;
import com.ubik.paymentservice.infrastructure.adapter.out.persistence.entity.PaymentEntity;

public class PaymentMapper {
    private PaymentMapper() {}

    public static Payment toDomain(PaymentEntity e) {
        return new Payment(
                e.id(), e.reservationId(), e.userId(), e.motelId(),
                e.amount(), e.currency(), PaymentStatus.valueOf(e.status()),
                e.mercadopagoPaymentId(), e.mercadopagoPreferenceId(),
                e.initPoint(), e.failureReason(), e.marketplaceFee(),
                e.createdAt(), e.updatedAt()
        );
    }

    public static PaymentEntity toEntity(Payment p) {
        return new PaymentEntity(
                p.id(), p.reservationId(), p.userId(), p.motelId(),
                p.amount(), p.currency(), p.status().name(),
                p.mercadopagoPaymentId(), p.mercadopagoPreferenceId(),
                p.initPoint(), p.failureReason(), p.marketplaceFee(),
                p.createdAt(), p.updatedAt()
        );
    }
}
