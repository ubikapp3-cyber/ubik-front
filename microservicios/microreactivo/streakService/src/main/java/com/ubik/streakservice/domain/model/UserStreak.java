package com.ubik.streakservice.domain.model;

import java.time.LocalDateTime;

/**
 * Value object del dominio. Sin dependencias de infraestructura.
 * Encapsula la lógica de clasificación.
 */
public record UserStreak(
        Long id,
        Long userId,
        StreakLevel level,
        int reservationsLast30Days,
        double discountRate,
        LocalDateTime calculatedAt,
        LocalDateTime updatedAt
) {
    /** Regla de negocio: clasifica según conteo mensual */
    public static StreakLevel calculateLevel(int reservationsLast30Days) {
        if (reservationsLast30Days >= 4) return StreakLevel.GOLD;
        if (reservationsLast30Days >= 2) return StreakLevel.AMATEUR;
        return StreakLevel.NEW;
    }

    /** Crea un snapshot nuevo (primer cálculo para el usuario) */
    public static UserStreak createNew(Long userId, int count) {
        StreakLevel level = calculateLevel(count);
        PrivilegePolicy policy = PrivilegePolicyFactory.getPolicy(level);
        return new UserStreak(null, userId, level, count,
                policy.discountRate(), LocalDateTime.now(), LocalDateTime.now());
    }

    /** Recalcula el nivel dado un nuevo conteo */
    public UserStreak recalculate(int newCount) {
        StreakLevel newLevel = calculateLevel(newCount);
        PrivilegePolicy policy = PrivilegePolicyFactory.getPolicy(newLevel);
        return new UserStreak(
                this.id(), this.userId(), newLevel, newCount,
                policy.discountRate(), LocalDateTime.now(), LocalDateTime.now());
    }

    /** Aplica el descuento a un precio base */
    public double applyDiscount(double basePrice) {
        return PrivilegePolicyFactory.getPolicy(this.level()).applyDiscount(basePrice);
    }
}
