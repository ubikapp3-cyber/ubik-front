package com.ubik.streakservice.domain.port.in;

import com.ubik.streakservice.domain.port.in.dto.AdminStreakStatsResponse;
import com.ubik.streakservice.domain.port.in.dto.AdminUserStreakResponse;
import com.ubik.streakservice.domain.port.in.dto.OverrideStreakRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StreakAdminUseCasePort {
    Flux<AdminUserStreakResponse> getAllStreaks(String levelParam);
    Mono<AdminStreakStatsResponse> getStreakStats();
    Mono<Void> overrideUserStreak(Long userId, OverrideStreakRequest request, Long adminId);
}
