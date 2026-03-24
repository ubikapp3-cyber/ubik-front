package com.ubik.streakservice.infrastructure.adapter.out.persistence;

import com.ubik.streakservice.domain.model.StreakLevel;
import com.ubik.streakservice.domain.model.UserStreak;
import com.ubik.streakservice.domain.port.out.StreakRepositoryPort;
import com.ubik.streakservice.infrastructure.adapter.out.persistence.entity.StreakEntity;
import com.ubik.streakservice.infrastructure.adapter.out.persistence.repository.StreakR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StreakPersistenceAdapter implements StreakRepositoryPort {

    private final StreakR2dbcRepository repository;

    public StreakPersistenceAdapter(StreakR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<UserStreak> findByUserId(Long userId) {
        return repository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Mono<UserStreak> save(UserStreak streak) {
        return repository.save(toEntity(streak)).map(this::toDomain);
    }

    private UserStreak toDomain(StreakEntity e) {
        return new UserStreak(
                e.id(), e.userId(),
                StreakLevel.valueOf(e.level()),
                e.reservationsLast30Days(),
                e.discountRate(),
                e.calculatedAt(), e.updatedAt()
        );
    }

    private StreakEntity toEntity(UserStreak s) {
        return new StreakEntity(
                s.id(), s.userId(),
                s.level().name(),
                s.reservationsLast30Days(),
                s.discountRate(),
                s.calculatedAt(), s.updatedAt()
        );
    }
}
