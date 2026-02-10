package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repositorio R2DBC para MotelEntity
 * Extiende ReactiveCrudRepository para operaciones CRUD reactivas
 */
@Repository
public interface MotelR2dbcRepository extends ReactiveCrudRepository<MotelEntity, Long> {

    /**
     * Busca moteles por ciudad
     * @param city Ciudad a buscar
     * @return Flux con las entidades encontradas
     */
    Flux<MotelEntity> findByCity(String city);

    /**
     * Busca moteles por propertyId (ID del propietario)
     * @param propertyId ID del propietario
     * @return Flux con las entidades encontradas
     */
    Flux<MotelEntity> findByPropertyId(Long propertyId);
}