package com.ubik.streakservice.domain;

import com.ubik.streakservice.domain.model.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class StreakLevelTest {

    @Test
    void usuario_con_1_reserva_es_NEW() {
        assertThat(UserStreak.calculateLevel(1)).isEqualTo(StreakLevel.NEW);
    }

    @Test
    void usuario_con_0_reservas_es_NEW() {
        assertThat(UserStreak.calculateLevel(0)).isEqualTo(StreakLevel.NEW);
    }

    @Test
    void usuario_con_3_reservas_es_AMATEUR() {
        assertThat(UserStreak.calculateLevel(3)).isEqualTo(StreakLevel.AMATEUR);
    }

    @Test
    void usuario_con_2_reservas_es_AMATEUR() {
        assertThat(UserStreak.calculateLevel(2)).isEqualTo(StreakLevel.AMATEUR);
    }

    @Test
    void usuario_con_5_reservas_es_GOLD() {
        assertThat(UserStreak.calculateLevel(5)).isEqualTo(StreakLevel.GOLD);
    }

    @Test
    void usuario_con_4_reservas_es_GOLD_borde() {
        assertThat(UserStreak.calculateLevel(4)).isEqualTo(StreakLevel.GOLD);
    }

    @Test
    void new_policy_no_aplica_descuento() {
        PrivilegePolicy policy = PrivilegePolicyFactory.getPolicy(StreakLevel.NEW);
        assertThat(policy.applyDiscount(100_000.0)).isEqualTo(100_000.0);
        assertThat(policy.discountRate()).isEqualTo(0.0);
    }

    @Test
    void amateur_policy_aplica_5_por_ciento() {
        PrivilegePolicy policy = PrivilegePolicyFactory.getPolicy(StreakLevel.AMATEUR);
        assertThat(policy.applyDiscount(100_000.0)).isEqualTo(95_000.0);
        assertThat(policy.discountRate()).isEqualTo(0.05);
    }

    @Test
    void gold_policy_aplica_10_por_ciento() {
        PrivilegePolicy policy = PrivilegePolicyFactory.getPolicy(StreakLevel.GOLD);
        assertThat(policy.applyDiscount(100_000.0)).isEqualTo(90_000.0);
        assertThat(policy.discountRate()).isEqualTo(0.10);
    }

    @Test
    void recalculo_puede_bajar_nivel() {
        UserStreak gold = UserStreak.createNew(1L, 5);
        assertThat(gold.level()).isEqualTo(StreakLevel.GOLD);

        UserStreak degraded = gold.recalculate(1);  // no reservó en 30 días
        assertThat(degraded.level()).isEqualTo(StreakLevel.NEW);
        assertThat(degraded.discountRate()).isEqualTo(0.0);
    }

    @Test
    void recalculo_puede_subir_nivel() {
        UserStreak newUser = UserStreak.createNew(1L, 0);
        assertThat(newUser.level()).isEqualTo(StreakLevel.NEW);

        UserStreak upgraded = newUser.recalculate(4);
        assertThat(upgraded.level()).isEqualTo(StreakLevel.GOLD);
        assertThat(upgraded.discountRate()).isEqualTo(0.10);
    }
}
