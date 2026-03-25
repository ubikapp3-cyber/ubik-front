package com.ubik.streakservice.domain;

import com.ubik.streakservice.domain.model.StreakLevel;
import com.ubik.streakservice.domain.model.UserStreak;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;

class UserStreakOverrideTest {

    @Test
    void should_respect_overridden_level_over_calculated_level() {
        // Arrange: Usuario NEW calculado (0 reservas)
        UserStreak streak = UserStreak.createNew(1L, 0);
        assertThat(streak.level()).isEqualTo(StreakLevel.NEW);
        assertThat(streak.getEffectiveLevel()).isEqualTo(StreakLevel.NEW);

        // Act: Admin fuerza nivel GOLD
        UserStreak overridden = streak.overrideLevel(StreakLevel.GOLD, "VIP Customer", 99L);

        // Assert
        assertThat(overridden.level()).isEqualTo(StreakLevel.NEW); // El calculado sigue siendo NEW
        assertThat(overridden.getEffectiveLevel()).isEqualTo(StreakLevel.GOLD); // El efectivo es GOLD
        assertThat(overridden.overriddenLevel()).isEqualTo(StreakLevel.GOLD);
        assertThat(overridden.overrideReason()).isEqualTo("VIP Customer");
        assertThat(overridden.updatedBy()).isEqualTo(99L);
    }

    @Test
    void should_apply_discount_of_overridden_level() {
        // Arrange
        UserStreak streak = UserStreak.createNew(1L, 0);
        UserStreak overridden = streak.overrideLevel(StreakLevel.GOLD, "Legacy user", 1L);

        // Act & Assert
        // Nivel GOLD tiene 10% de descuento
        assertThat(overridden.applyDiscount(100.0)).isEqualTo(90.0);
    }
    
    @Test
    void recalculate_should_preserve_override_info() {
        // Arrange
        UserStreak streak = UserStreak.createNew(1L, 0)
                .overrideLevel(StreakLevel.GOLD, "Bug compensation", 1L);
        
        // Act: El sistema recalcula porque el usuario hizo 2 reservas (sería AMATEUR)
        UserStreak recalculated = streak.recalculate(2);
        
        // Assert
        assertThat(recalculated.level()).isEqualTo(StreakLevel.AMATEUR); // Nivel base subió
        assertThat(recalculated.getEffectiveLevel()).isEqualTo(StreakLevel.GOLD); // Sigue mandando el override
        assertThat(recalculated.overriddenLevel()).isEqualTo(StreakLevel.GOLD);
        assertThat(recalculated.overrideReason()).isEqualTo("Bug compensation");
    }
}
