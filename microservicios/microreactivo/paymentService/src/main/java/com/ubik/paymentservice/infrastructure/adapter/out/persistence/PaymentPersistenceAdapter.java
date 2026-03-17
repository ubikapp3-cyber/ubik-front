package com.ubik.paymentservice.infrastructure.adapter.out.persistence;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.model.PaymentStatus;
import com.ubik.paymentservice.domain.port.out.PaymentRepositoryPort;
import com.ubik.paymentservice.infrastructure.adapter.out.persistence.mapper.PaymentMapper;
import com.ubik.paymentservice.infrastructure.adapter.out.persistence.repository.PaymentR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Adaptador de persistencia que implementa PaymentRepositoryPort.
 * Sigue el mismo patrón que ReservationPersistenceAdapter en motelManagement.
 */
@Component
public class PaymentPersistenceAdapter implements PaymentRepositoryPort {

    private final PaymentR2dbcRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentPersistenceAdapter(
            PaymentR2dbcRepository paymentRepository,
            PaymentMapper paymentMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public Mono<Payment> save(Payment payment) {
        return Mono.just(payment)
                .map(paymentMapper::toEntity)
                .flatMap(paymentRepository::save)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Mono<Payment> findById(Long id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Flux<Payment> findByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Flux<Payment> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Mono<Payment> findByStripePaymentIntentId(String stripePaymentIntentId) {
        return paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Mono<Payment> updateStatus(String stripePaymentIntentId, PaymentStatus status, String failureMessage) {
        return paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .switchIfEmpty(Mono.error(new RuntimeException(
                        "Pago no encontrado para el PaymentIntent: " + stripePaymentIntentId)))
                .flatMap(entity -> {
                    // Creamos una nueva entidad con el status y updatedAt actualizados
                    var updatedEntity = new com.ubik.paymentservice.infrastructure.adapter.out.persistence.entity.PaymentEntity(
                            entity.id(),
                            entity.reservationId(),
                            entity.userId(),
                            entity.motelId(),
                            entity.amount(),
                            entity.stripePaymentIntentId(),
                            entity.amountCents(),
                            entity.currency(),
                            status.name(),
                            failureMessage,
                            entity.createdAt(),
                            LocalDateTime.now()
                    );
                    return paymentRepository.save(updatedEntity);
                })
                .map(paymentMapper::toDomain);
    }
}
