package com.ubik.paymentservice.infrastructure.adapter.out.persistence.repository;

import com.ubik.paymentservice.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC reactivo para la tabla "payments"
 */
@Repository
public interface PaymentR2dbcRepository extends ReactiveCrudRepository<PaymentEntity, Long> {

    Flux<PaymentEntity> findByReservationId(Long reservationId);

    Flux<PaymentEntity> findByUserId(Long userId);

    Mono<PaymentEntity> findByStripePaymentIntentId(String stripePaymentIntentId);
}
