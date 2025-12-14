package com.ubik.motelmanagement.domain.port.out;

import com.ubik.motelmanagement.domain.model.Room;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (Output Port) para operaciones de persistencia de Room
 * Define el contrato que debe implementar la infraestructura
 */
public interface RoomRepositoryPort {

    /**
     * Guarda una nueva habitación
     * @param room Habitación a guardar
     * @return Mono con la habitación guardada incluyendo su ID generado
     */
    Mono<Room> save(Room room);

    /**
     * Busca una habitación por su ID
     * @param id ID de la habitación
     * @return Mono con la habitación encontrada o vacío
     */
    Mono<Room> findById(Long id);

    /**
     * Busca todas las habitaciones
     * @return Flux con todas las habitaciones
     */
    Flux<Room> findAll();

    /**
     * Busca habitaciones por ID de motel
     * @param motelId ID del motel
     * @return Flux con las habitaciones del motel
     */
    Flux<Room> findByMotelId(Long motelId);

    /**
     * Busca habitaciones disponibles por ID de motel
     * @param motelId ID del motel
     * @return Flux con las habitaciones disponibles
     */
    Flux<Room> findAvailableByMotelId(Long motelId);

    /**
     * Actualiza una habitación existente
     * @param room Habitación con los datos actualizados
     * @return Mono con la habitación actualizada
     */
    Mono<Room> update(Room room);

    /**
     * Elimina una habitación por su ID
     * @param id ID de la habitación a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteById(Long id);

    /**
     * Verifica si existe una habitación con el ID dado
     * @param id ID de la habitación
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsById(Long id);
}