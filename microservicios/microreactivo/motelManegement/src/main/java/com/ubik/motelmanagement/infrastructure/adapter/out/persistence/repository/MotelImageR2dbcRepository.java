package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelImageEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para gestionar imágenes de moteles
 */
@Repository
public interface MotelImageR2dbcRepository extends R2dbcRepository<MotelImageEntity, Integer> {

    Flux<MotelImageEntity> findByMotelIdOrderByOrderIndexAsc(Integer motelId);

    Mono<Void> deleteByMotelId(Integer motelId);
}
