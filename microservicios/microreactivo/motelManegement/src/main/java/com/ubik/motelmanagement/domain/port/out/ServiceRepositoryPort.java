package com.ubik.motelmanagement.domain.port.out;

import com.ubik.motelmanagement.domain.model.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (Output Port) para operaciones de persistencia de Service
 * Define el contrato que debe implementar la infraestructura
 */
public interface ServiceRepositoryPort {

    /**
     * Guarda un nuevo servicio
     * @param service Servicio a guardar
     * @return Mono con el servicio guardado incluyendo su ID generado
     */
    Mono<Service> save(Service service);

    /**
     * Busca un servicio por su ID
     * @param id ID del servicio
     * @return Mono con el servicio encontrado o vacío
     */
    Mono<Service> findById(Long id);

    /**
     * Busca todos los servicios
     * @return Flux con todos los servicios
     */
    Flux<Service> findAll();

    /**
     * Busca un servicio por su nombre
     * @param name Nombre del servicio
     * @return Mono con el servicio encontrado o vacío
     */
    Mono<Service> findByName(String name);

    /**
     * Actualiza un servicio existente
     * @param service Servicio con los datos actualizados
     * @return Mono con el servicio actualizado
     */
    Mono<Service> update(Service service);

    /**
     * Elimina un servicio por su ID
     * @param id ID del servicio a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteById(Long id);

    /**
     * Verifica si existe un servicio con el ID dado
     * @param id ID del servicio
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsById(Long id);

    /**
     * Verifica si existe un servicio con el nombre dado
     * @param name Nombre del servicio
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsByName(String name);

    /**
     * Obtiene los IDs de servicios asociados a una habitación
     * @param roomId ID de la habitación
     * @return Flux con los IDs de servicios
     */
    Flux<Long> findServiceIdsByRoomId(Long roomId);

    /**
     * Asocia un servicio a una habitación
     * @param roomId ID de la habitación
     * @param serviceId ID del servicio
     * @return Mono vacío que completa cuando se asocia
     */
    Mono<Void> addServiceToRoom(Long roomId, Long serviceId);

    /**
     * Elimina un servicio de una habitación
     * @param roomId ID de la habitación
     * @param serviceId ID del servicio
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> removeServiceFromRoom(Long roomId, Long serviceId);

    /**
     * Verifica si existe la relación entre una habitación y un servicio
     * @param roomId ID de la habitación
     * @param serviceId ID del servicio
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsRoomServiceRelation(Long roomId, Long serviceId);
}
