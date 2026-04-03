package com.ubik.streakservice.domain.service;

import com.ubik.streakservice.domain.model.UserStreak;
import com.ubik.streakservice.domain.port.in.StreakUseCasePort;
import com.ubik.streakservice.domain.port.out.ReservationQueryPort;
import com.ubik.streakservice.domain.port.out.StreakRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
public class StreakService implements StreakUseCasePort {

    private static final Logger log = LoggerFactory.getLogger(StreakService.class);

    private final StreakRepositoryPort streakRepository;
    private final ReservationQueryPort reservationQuery;
    private final TransactionalOperator tx;

    public StreakService(StreakRepositoryPort streakRepository,
                         ReservationQueryPort reservationQuery,
                         TransactionalOperator tx) {
        this.streakRepository = streakRepository;
        this.reservationQuery = reservationQuery;
        this.tx = tx;
    }

    @Override
    public Mono<UserStreak> getStreak(Long userId) {
        return streakRepository.findByUserId(userId)
                .switchIfEmpty(recalculate(userId))   // primera consulta: calcular fresh
                .doOnSuccess(s -> log.debug("Streak para userId={}: level={}, count={}",
                        userId, s.level(), s.reservationsLast30Days()));
    }

    @Override
    public Mono<UserStreak> recalculate(Long userId) {
        return tx.transactional(
            reservationQuery.countLast30Days(userId)
                .flatMap(count -> streakRepository.findByUserId(userId)
                        .map(existing -> existing.recalculate(count))
                        .switchIfEmpty(Mono.just(UserStreak.createNew(userId, count))))
                .flatMap(streakRepository::save)
                .doOnSuccess(s -> log.info("Racha recalculada userId={} → level={} ({}r/30d)",
                        userId, s.level(), s.reservationsLast30Days()))
        );
    }

    @Override
    public Mono<Double> applyDiscount(Long userId, double basePrice) {
        return getStreak(userId)
                .map(streak -> streak.applyDiscount(basePrice));
    }
}
