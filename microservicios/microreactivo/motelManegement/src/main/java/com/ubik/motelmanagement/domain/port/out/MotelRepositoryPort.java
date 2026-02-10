package com.ubik.motelmanagement.domain.port.out;

import com.ubik.motelmanagement.domain.model.Motel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (Output Port) para operaciones de persistencia de Motel
 * Define el contrato que debe implementar la infraestructura
 * Parte de la arquitectura hexagonal
 */
public interface MotelRepositoryPort {

    /**
     * Guarda un nuevo motel
     * @param motel Motel a guardar
     * @return Mono con el motel guardado incluyendo su ID generado
     */
    Mono<Motel> save(Motel motel);

    /**
     * Busca un motel por su ID
     * @param id ID del motel
     * @return Mono con el motel encontrado o vacío
     */
    Mono<Motel> findById(Long id);

    /**
     * Busca todos los moteles
     * @return Flux con todos los moteles
     */
    Flux<Motel> findAll();

    /**
     * Busca moteles por ciudad
     * @param city Ciudad a buscar
     * @return Flux con los moteles de esa ciudad
     */
    Flux<Motel> findByCity(String city);

    /**
     * Busca moteles por propertyId (ID del propietario)
     * @param propertyId ID del propietario
     * @return Flux con los moteles de ese propietario
     */
    Flux<Motel> findByPropertyId(Long propertyId);

    /**
     * Actualiza un motel existente
     * @param motel Motel con los datos actualizados
     * @return Mono con el motel actualizado
     */
    Mono<Motel> update(Motel motel);

    /**
     * Elimina un motel por su ID
     * @param id ID del motel a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteById(Long id);

    /**
     * Verifica si existe un motel con el ID dado
     * @param id ID del motel
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsById(Long id);
}