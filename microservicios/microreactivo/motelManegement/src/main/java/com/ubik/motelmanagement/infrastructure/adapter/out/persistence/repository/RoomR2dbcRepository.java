package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.RoomEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repositorio R2DBC para RoomEntity
 * Extiende ReactiveCrudRepository para operaciones CRUD reactivas
 */
@Repository
public interface RoomR2dbcRepository extends ReactiveCrudRepository<RoomEntity, Long> {

    /**
     * Busca habitaciones por ID de motel
     * @param motelId ID del motel
     * @return Flux con las habitaciones encontradas
     */
    Flux<RoomEntity> findByMotelId(Long motelId);

    /**
     * Busca habitaciones disponibles por ID de motel
     * @param motelId ID del motel
     * @param isAvailable Estado de disponibilidad
     * @return Flux con las habitaciones disponibles
     */
    Flux<RoomEntity> findByMotelIdAndIsAvailable(Long motelId, Boolean isAvailable);
}