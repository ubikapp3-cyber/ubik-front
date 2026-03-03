package com.ubik.paymentservice.infrastructure.adapter.out.persistence;

import com.ubik.paymentservice.domain.model.Payment;
import com.ubik.paymentservice.domain.port.out.PaymentRepository;
import com.ubik.paymentservice.infrastructure.adapter.out.persistence.mapper.PaymentMapper;
import com.ubik.paymentservice.infrastructure.adapter.out.persistence.repository.PaymentR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentR2dbcRepository r2dbc;

    public PaymentRepositoryAdapter(PaymentR2dbcRepository r2dbc) {
        this.r2dbc = r2dbc;
    }

    @Override public Mono<Payment> save(Payment p)                         { return r2dbc.save(PaymentMapper.toEntity(p)).map(PaymentMapper::toDomain); }
    @Override public Mono<Payment> findById(Long id)                        { return r2dbc.findById(id).map(PaymentMapper::toDomain); }
    @Override public Mono<Payment> findByMercadopagoPaymentId(String mpId)  { return r2dbc.findByMercadopagoPaymentId(mpId).map(PaymentMapper::toDomain); }
    @Override public Mono<Payment> findLatestByReservationId(Long resId)    { return r2dbc.findFirstByReservationIdOrderByCreatedAtDesc(resId).map(PaymentMapper::toDomain); }
    @Override public Flux<Payment> findAllByReservationId(Long resId)       { return r2dbc.findAllByReservationId(resId).map(PaymentMapper::toDomain); }
    @Override public Flux<Payment> findAllByUserId(Long userId)             { return r2dbc.findAllByUserId(userId).map(PaymentMapper::toDomain); }
}
