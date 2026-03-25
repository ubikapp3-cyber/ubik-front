package com.ubik.streakservice.domain.service;

import com.ubik.streakservice.domain.model.StreakLevel;
import com.ubik.streakservice.domain.model.UserStreak;
import com.ubik.streakservice.domain.port.out.StreakRepositoryPort;
import com.ubik.streakservice.domain.port.out.UserQueryPort;
import com.ubik.streakservice.domain.port.in.dto.AdminStreakStatsResponse;
import com.ubik.streakservice.domain.port.in.dto.OverrideStreakRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreakAdminServiceTest {

    @Mock
    private StreakRepositoryPort streakRepository;

    @Mock
    private UserQueryPort userQueryPort;

    @InjectMocks
    private StreakAdminService streakAdminService;

    @Test
    void shouldCalculateStatsCorrectlyIncludingOverrides() {
        // Arrange
        // Usuario 1: NEW (0 reservas)
        UserStreak s1 = UserStreak.createNew(1L, 0);
        // Usuario 2: AMATEUR (2 reservas)
        UserStreak s2 = UserStreak.createNew(2L, 2);
        // Usuario 3: NEW (1 r), pero Admin fuerza GOLD
        UserStreak s3 = UserStreak.createNew(3L, 1).overrideLevel(StreakLevel.GOLD, "VIP", 99L);
        // Usuario 4: GOLD (5 reservas), top user
        UserStreak s4 = UserStreak.createNew(4L, 5);

        when(streakRepository.findAll()).thenReturn(Flux.just(s1, s2, s3, s4));

        // Act & Assert
        StepVerifier.create(streakAdminService.getStreakStats())
            .expectNextMatches(stats -> 
                stats.totalUsers() == 4 &&
                stats.newUsers() == 1 &&      // s1 únicamente
                stats.amateurUsers() == 1 &&  // s2 únicamente
                stats.goldUsers() == 2 &&     // s3 (overridden) + s4 (orgánico)
                stats.topUserId().equals(4L) &&
                stats.topReservations() == 5
            )
            .verifyComplete();
    }

    @Test
    void shouldOverrideUserStreakSuccessfully() {
        // Arrange
        Long userId = 1L;
        Long adminId = 99L;
        UserStreak originalStreak = UserStreak.createNew(userId, 0);
        
        when(streakRepository.findByUserId(userId)).thenReturn(Mono.just(originalStreak));
        when(streakRepository.save(any(UserStreak.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        OverrideStreakRequest request = new OverrideStreakRequest("GOLD", "Compensation for issue");

        // Act & Assert
        StepVerifier.create(streakAdminService.overrideUserStreak(userId, request, adminId))
            .verifyComplete();

        verify(streakRepository).save(argThat(streak -> 
            streak.overriddenLevel() == StreakLevel.GOLD &&
            "Compensation for issue".equals(streak.overrideReason()) &&
            adminId.equals(streak.updatedBy())
        ));
    }
}
