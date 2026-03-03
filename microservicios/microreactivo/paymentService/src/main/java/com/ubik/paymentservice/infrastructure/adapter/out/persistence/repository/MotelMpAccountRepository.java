package com.ubik.paymentservice.infrastructure.adapter.out.persistence.repository;

import com.ubik.paymentservice.infrastructure.adapter.out.persistence.entity.MotelMpAccountEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MotelMpAccountRepository extends R2dbcRepository<MotelMpAccountEntity, Long> {
    Mono<MotelMpAccountEntity> findByMotelId(Long motelId);
}
