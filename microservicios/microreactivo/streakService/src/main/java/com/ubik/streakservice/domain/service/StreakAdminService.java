package com.ubik.streakservice.domain.service;

import com.ubik.streakservice.domain.model.StreakLevel;
import com.ubik.streakservice.domain.model.UserStreak;
import com.ubik.streakservice.domain.model.UserSummary;
import com.ubik.streakservice.domain.port.in.StreakAdminUseCasePort;
import com.ubik.streakservice.domain.port.out.StreakRepositoryPort;
import com.ubik.streakservice.domain.port.out.UserQueryPort;
import com.ubik.streakservice.domain.port.in.dto.AdminStreakStatsResponse;
import com.ubik.streakservice.domain.port.in.dto.AdminUserStreakResponse;
import com.ubik.streakservice.domain.port.in.dto.OverrideStreakRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class StreakAdminService implements StreakAdminUseCasePort {

    private final StreakRepositoryPort streakRepository;
    private final UserQueryPort userQueryPort;

    public StreakAdminService(StreakRepositoryPort streakRepository, UserQueryPort userQueryPort) {
        this.streakRepository = streakRepository;
        this.userQueryPort = userQueryPort;
    }

    @Override
    public Flux<AdminUserStreakResponse> getAllStreaks(String levelParam) {
        Flux<UserStreak> streaks = (levelParam != null && !levelParam.isBlank()) 
            ? streakRepository.findByEffectiveLevel(levelParam) // o mapear a StreakLevel si es necesario, pero r2dbcRepository usa String
            : streakRepository.findAll();

        return streaks.flatMap(streak -> 
            userQueryPort.getUserSummary(streak.userId())
                .map(user -> buildAdminResponse(streak, user))
        );
    }

    @Override
    public Mono<AdminStreakStatsResponse> getStreakStats() {
        return streakRepository.findAll()
            .collectList()
            .map(streaks -> {
                long total = streaks.size();
                if (total == 0) return new AdminStreakStatsResponse(0,0,0,0,0.0,0.0,0.0,null,0);

                long newUsers = streaks.stream().filter(s -> s.getEffectiveLevel() == StreakLevel.NEW).count();
                long amateurUsers = streaks.stream().filter(s -> s.getEffectiveLevel() == StreakLevel.AMATEUR).count();
                long goldUsers = streaks.stream().filter(s -> s.getEffectiveLevel() == StreakLevel.GOLD).count();

                UserStreak topUser = streaks.stream()
                    .max(Comparator.comparingInt(UserStreak::reservationsLast30Days))
                    .orElse(null);

                return new AdminStreakStatsResponse(
                    total, newUsers, amateurUsers, goldUsers,
                    (double) newUsers / total * 100,
                    (double) amateurUsers / total * 100,
                    (double) goldUsers / total * 100,
                    topUser != null ? topUser.userId() : null,
                    topUser != null ? topUser.reservationsLast30Days() : 0
                );
            });
    }

    @Override
    public Mono<Void> overrideUserStreak(Long userId, OverrideStreakRequest request, Long adminId) {
        return streakRepository.findByUserId(userId)
            .switchIfEmpty(Mono.error(new RuntimeException("Streak not found for user: " + userId)))
            .flatMap(streak -> {
                UserStreak updatedStreak = streak.overrideLevel(
                        StreakLevel.valueOf(request.level()), 
                        request.overrideReason(), 
                        adminId);
                return streakRepository.save(updatedStreak);
            })
            .then();
    }

    private AdminUserStreakResponse buildAdminResponse(UserStreak streak, UserSummary user) {
        return new AdminUserStreakResponse(
            streak.userId(),
            user.username(),
            user.email(),
            streak.getEffectiveLevel().name(),
            streak.reservationsLast30Days(),
            streak.discountRate(),
            streak.calculatedAt(),
            streak.overriddenLevel() != null ? streak.overriddenLevel().name() : null,
            streak.overrideReason(),
            streak.updatedBy()
        );
    }
}
