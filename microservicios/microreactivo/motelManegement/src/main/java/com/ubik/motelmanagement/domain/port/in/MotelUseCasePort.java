package com.ubik.motelmanagement.domain.port.in;

import com.ubik.motelmanagement.domain.model.Motel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (Input Port) para casos de uso de Motel
 * Define las operaciones disponibles desde la capa de aplicación
 */
public interface MotelUseCasePort {

    /**
     * Crea un nuevo motel
     * @param motel Motel a crear
     * @return Mono con el motel creado
     */
    Mono<Motel> createMotel(Motel motel);

    /**
     * Obtiene un motel por su ID
     * @param id ID del motel
     * @return Mono con el motel encontrado
     */
    Mono<Motel> getMotelById(Long id);

    /**
     * Obtiene todos los moteles
     * @return Flux con todos los moteles
     */
    Flux<Motel> getAllMotels();

    /**
     * Obtiene moteles por ciudad
     * @param city Ciudad a buscar
     * @return Flux con los moteles de esa ciudad
     */
    Flux<Motel> getMotelsByCity(String city);

    /**
     * Actualiza un motel existente
     * @param id ID del motel a actualizar
     * @param motel Datos actualizados del motel
     * @return Mono con el motel actualizado
     */
    Mono<Motel> updateMotel(Long id, Motel motel);

    /**
     * Elimina un motel
     * @param id ID del motel a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteMotel(Long id);
}