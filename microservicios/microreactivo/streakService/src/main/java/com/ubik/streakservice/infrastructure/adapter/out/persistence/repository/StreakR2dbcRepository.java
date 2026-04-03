package com.ubik.streakservice.infrastructure.adapter.out.persistence.repository;

import com.ubik.streakservice.infrastructure.adapter.out.persistence.entity.StreakEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StreakR2dbcRepository extends R2dbcRepository<StreakEntity, Long> {
    Mono<StreakEntity> findByUserId(Long userId);

    @Query("SELECT * FROM user_streaks WHERE COALESCE(overridden_level, level) = :level")
    Flux<StreakEntity> findByEffectiveLevel(String level);
}
