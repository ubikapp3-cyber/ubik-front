package com.ubik.motelmanagement.domain.port.in;

import com.ubik.motelmanagement.domain.model.Room;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (Input Port) para casos de uso de Room
 * Define las operaciones disponibles desde la capa de aplicación
 */
public interface RoomUseCasePort {

    /**
     * Crea una nueva habitación
     * @param room Habitación a crear
     * @return Mono con la habitación creada
     */
    Mono<Room> createRoom(Room room);

    /**
     * Obtiene una habitación por su ID
     * @param id ID de la habitación
     * @return Mono con la habitación encontrada
     */
    Mono<Room> getRoomById(Long id);

    /**
     * Obtiene todas las habitaciones
     * @return Flux con todas las habitaciones
     */
    Flux<Room> getAllRooms();

    /**
     * Obtiene habitaciones por ID de motel
     * @param motelId ID del motel
     * @return Flux con las habitaciones del motel
     */
    Flux<Room> getRoomsByMotelId(Long motelId);

    /**
     * Obtiene habitaciones disponibles por ID de motel
     * @param motelId ID del motel
     * @return Flux con las habitaciones disponibles
     */
    Flux<Room> getAvailableRoomsByMotelId(Long motelId);

    /**
     * Actualiza una habitación existente
     * @param id ID de la habitación a actualizar
     * @param room Datos actualizados de la habitación
     * @return Mono con la habitación actualizada
     */
    Mono<Room> updateRoom(Long id, Room room);

    /**
     * Elimina una habitación
     * @param id ID de la habitación a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteRoom(Long id);
}