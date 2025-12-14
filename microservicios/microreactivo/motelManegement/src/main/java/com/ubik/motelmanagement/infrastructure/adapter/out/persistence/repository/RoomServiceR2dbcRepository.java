package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.RoomServiceEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para RoomServiceEntity
 * Gestiona la relación muchos-a-muchos entre Room y Service
 */
@Repository
public interface RoomServiceR2dbcRepository extends ReactiveCrudRepository<RoomServiceEntity, Long> {

    /**
     * Obtiene todos los servicios de una habitación
     * @param roomId ID de la habitación
     * @return Flux con las relaciones encontradas
     */
    Flux<RoomServiceEntity> findByRoomId(Long roomId);

    /**
     * Obtiene todas las habitaciones que tienen un servicio específico
     * @param serviceId ID del servicio
     * @return Flux con las relaciones encontradas
     */
    Flux<RoomServiceEntity> findByServiceId(Long serviceId);

    /**
     * Elimina todos los servicios de una habitación
     * @param roomId ID de la habitación
     * @return Mono vacío que completa cuando se eliminan
     */
    Mono<Void> deleteByRoomId(Long roomId);

    /**
     * Elimina un servicio específico de una habitación
     * @param roomId ID de la habitación
     * @param serviceId ID del servicio
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteByRoomIdAndServiceId(Long roomId, Long serviceId);

    /**
     * Verifica si existe una relación entre habitación y servicio
     */
    @Query("SELECT EXISTS(SELECT 1 FROM room_service WHERE room_id = :roomId AND service_id = :serviceId)")
    Mono<Boolean> existsByRoomIdAndServiceId(Long roomId, Long serviceId);


}
