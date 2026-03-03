package com.ubik.paymentservice.domain.port.out;

import com.ubik.paymentservice.domain.model.Payment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentRepository {
    Mono<Payment> save(Payment payment);
    Mono<Payment> findById(Long id);
    Mono<Payment> findByMercadopagoPaymentId(String mpPaymentId);
    Mono<Payment> findLatestByReservationId(Long reservationId);
    Flux<Payment> findAllByReservationId(Long reservationId);
    Flux<Payment> findAllByUserId(Long userId);
}
