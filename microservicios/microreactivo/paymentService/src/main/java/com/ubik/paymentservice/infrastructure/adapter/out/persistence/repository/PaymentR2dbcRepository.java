package com.ubik.paymentservice.infrastructure.adapter.out.persistence.repository;

import com.ubik.paymentservice.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentR2dbcRepository extends R2dbcRepository<PaymentEntity, Long> {
    Mono<PaymentEntity> findByMercadopagoPaymentId(String mpPaymentId);
    Mono<PaymentEntity> findFirstByReservationIdOrderByCreatedAtDesc(Long reservationId);
    Flux<PaymentEntity> findAllByReservationId(Long reservationId);
    Flux<PaymentEntity> findAllByUserId(Long userId);
}
