package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.RoomImageEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para gestionar imágenes de habitaciones
 */
@Repository
public interface RoomImageR2dbcRepository extends R2dbcRepository<RoomImageEntity, Integer> {

    Flux<RoomImageEntity> findByRoomIdOrderByOrderIndexAsc(Integer roomId);

    Mono<Void> deleteByRoomId(Integer roomId);
}
