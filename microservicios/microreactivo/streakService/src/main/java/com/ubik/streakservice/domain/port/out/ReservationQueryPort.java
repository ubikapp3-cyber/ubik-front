package com.ubik.streakservice.domain.port.out;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida: consulta cuántas reservas hizo el usuario en los últimos 30 días.
 * Implementado por un WebClient apuntando a motel-management-service.
 */
public interface ReservationQueryPort {
    Mono<Integer> countLast30Days(Long userId);
}
