package com.ubik.paymentservice.infrastructure.adapter.out.persistence.mapper;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.model.PaymentStatus;
import com.ubik.paymentservice.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre el modelo de dominio Payment y la entidad de persistencia PaymentEntity.
 * Sigue el mismo patrón que ReservationMapper en motelManagement.
 */
@Component
public class PaymentMapper {

    public Payment toDomain(PaymentEntity entity) {
        if (entity == null) return null;

        return new Payment(
                entity.id(),
                entity.reservationId(),
                entity.userId(),
                entity.stripePaymentIntentId(),
                entity.amountCents(),
                entity.currency(),
                PaymentStatus.valueOf(entity.status()),
                entity.failureMessage(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

    public PaymentEntity toEntity(Payment payment) {
        if (payment == null) return null;

        return new PaymentEntity(
                payment.id(),
                payment.reservationId(),
                payment.userId(),
                0L, // dummy motel_id para compatibilidad con BD
                payment.amountCents() / 100L, // legacy 'amount' (pesos COP) para compatibilidad con BD
                payment.stripePaymentIntentId(),
                payment.amountCents(),
                payment.currency(),
                payment.status().name(),
                payment.failureMessage(),
                payment.createdAt(),
                payment.updatedAt()
        );
    }
}
