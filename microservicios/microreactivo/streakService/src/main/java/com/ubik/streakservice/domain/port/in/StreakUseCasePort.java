package com.ubik.streakservice.domain.port.in;

import com.ubik.streakservice.domain.model.UserStreak;
import reactor.core.publisher.Mono;

public interface StreakUseCasePort {

    /** Obtiene (o calcula fresh) la racha de un usuario */
    Mono<UserStreak> getStreak(Long userId);

    /**
     * Fuerza el recálculo al ocurrir una reserva.
     * Lo llama motel-management después de confirmar la reserva.
     */
    Mono<UserStreak> recalculate(Long userId);

    /** Calcula el precio final con descuento aplicado */
    Mono<Double> applyDiscount(Long userId, double basePrice);
}
