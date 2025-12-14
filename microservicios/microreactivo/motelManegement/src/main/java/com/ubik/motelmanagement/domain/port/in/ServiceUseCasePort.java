package com.ubik.motelmanagement.domain.port.in;

import com.ubik.motelmanagement.domain.model.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (Input Port) para casos de uso de Service
 * Define las operaciones disponibles desde la capa de aplicación
 */
public interface ServiceUseCasePort {

    /**
     * Crea un nuevo servicio
     * @param service Servicio a crear
     * @return Mono con el servicio creado
     */
    Mono<Service> createService(Service service);

    /**
     * Obtiene un servicio por su ID
     * @param id ID del servicio
     * @return Mono con el servicio encontrado
     */
    Mono<Service> getServiceById(Long id);

    /**
     * Obtiene todos los servicios
     * @return Flux con todos los servicios
     */
    Flux<Service> getAllServices();

    /**
     * Obtiene un servicio por su nombre
     * @param name Nombre del servicio
     * @return Mono con el servicio encontrado
     */
    Mono<Service> getServiceByName(String name);

    /**
     * Actualiza un servicio existente
     * @param id ID del servicio a actualizar
     * @param service Datos actualizados del servicio
     * @return Mono con el servicio actualizado
     */
    Mono<Service> updateService(Long id, Service service);

    /**
     * Elimina un servicio
     * @param id ID del servicio a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteService(Long id);

    /**
     * Obtiene los IDs de servicios asociados a una habitación
     * @param roomId ID de la habitación
     * @return Flux con los IDs de servicios
     */
    Flux<Long> getServiceIdsByRoomId(Long roomId);

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
}
