package com.ubik.streakservice.domain.port.out;

import com.ubik.streakservice.domain.model.UserStreak;
import reactor.core.publisher.Mono;

public interface StreakRepositoryPort {
    Mono<UserStreak> findByUserId(Long userId);
    Mono<UserStreak> save(UserStreak streak);
}
