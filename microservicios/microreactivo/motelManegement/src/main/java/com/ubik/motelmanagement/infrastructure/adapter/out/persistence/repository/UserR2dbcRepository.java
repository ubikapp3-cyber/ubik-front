package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.UserBasicEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para consultar información básica de Users
 * (solo lectura, para obtener userId desde username)
 */
@Repository
public interface UserR2dbcRepository extends R2dbcRepository<UserBasicEntity, Long> {

    /**
     * Busca un usuario por username y devuelve solo su ID
     */
    @Query("SELECT id FROM users WHERE username = :username")
    Mono<Long> findIdByUsername(@Param("username") String username);

    /**
     * Busca un usuario por username y devuelve la entidad básica
     */
    Mono<UserBasicEntity> findByUsername(String username);
}