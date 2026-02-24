package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.ReservationCounterEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Repositorio para gestionar contadores de códigos de confirmación
 */
@Repository
public interface ReservationCounterRepository extends R2dbcRepository<ReservationCounterEntity, LocalDate> {

    /**
     * Incrementa el contador de forma atómica y devuelve el nuevo valor
     * Usa UPDATE + RETURNING para garantizar atomicidad
     */
    @Modifying
    @Query("INSERT INTO reservation_counters (date, counter, created_at, updated_at) " +
            "VALUES (:date, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON CONFLICT (date) " +
            "DO UPDATE SET counter = reservation_counters.counter + 1, " +
            "              updated_at = CURRENT_TIMESTAMP " +
            "RETURNING counter")
    Mono<Integer> incrementAndGet(LocalDate date);

    /**
     * Obtiene el contador actual para una fecha (sin incrementar)
     */
    @Query("SELECT counter FROM reservation_counters WHERE date = :date")
    Mono<Integer> getCurrentCounter(LocalDate date);
}