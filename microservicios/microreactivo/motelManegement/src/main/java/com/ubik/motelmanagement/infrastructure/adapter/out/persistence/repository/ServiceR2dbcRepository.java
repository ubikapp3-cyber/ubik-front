package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.ServiceEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para ServiceEntity
 * Extiende ReactiveCrudRepository para operaciones CRUD reactivas
 */
@Repository
public interface ServiceR2dbcRepository extends ReactiveCrudRepository<ServiceEntity, Long> {

    /**
     * Busca un servicio por su nombre
     * @param name Nombre del servicio
     * @return Mono con el servicio encontrado
     */
    Mono<ServiceEntity> findByName(String name);

    /**
     * Verifica si existe un servicio con el nombre dado
     * @param name Nombre del servicio
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsByName(String name);
}
