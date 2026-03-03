package com.example.paymentservice.repository;

import com.example.paymentservice.entity.PaymentEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentRepository extends R2dbcRepository<PaymentEntity, Long> {

    Flux<PaymentEntity> findByReservationId(Long reservationId);

    Flux<PaymentEntity> findByUserId(Long userId);

    @Query("SELECT * FROM payments WHERE mercadopago_payment_id = :mpPaymentId")
    Mono<PaymentEntity> findByMercadoPagoPaymentId(String mpPaymentId);

    @Query("SELECT * FROM payments WHERE mercadopago_preference_id = :preferenceId")
    Mono<PaymentEntity> findByMercadoPagoPreferenceId(String preferenceId);
}