package com.ubik.streakservice.infrastructure.adapter.out.persistence.repository;

import com.ubik.streakservice.infrastructure.adapter.out.persistence.entity.StreakEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface StreakR2dbcRepository extends R2dbcRepository<StreakEntity, Long> {
    Mono<StreakEntity> findByUserId(Long userId);
}
