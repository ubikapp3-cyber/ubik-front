package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelImageEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para gestionar imágenes de moteles
 *
 * Tabla: motel_images
 * Columnas: id, motel_id, image_url, order_index, role
 */
@Repository
public interface MotelImageR2dbcRepository extends R2dbcRepository<MotelImageEntity, Integer> {

    /**
     * Ordena: PROFILE primero, COVER segundo, luego GALLERY por order_index.
     */
    @Query("""
        SELECT *
        FROM motel_images
        WHERE motel_id = :motelId
        ORDER BY
          CASE role
            WHEN 'PROFILE' THEN 1
            WHEN 'COVER' THEN 2
            ELSE 3
          END,
          order_index NULLS LAST,
          id
    """)
    Flux<MotelImageEntity> findOrderedByMotelId(@Param("motelId") Integer motelId);

    /**
     * Mantengo tu método anterior por compatibilidad (si lo usas en otros lados).
     * (Solo orden por order_index)
     */
    Flux<MotelImageEntity> findByMotelIdOrderByOrderIndexAsc(Integer motelId);

    /**
     * Borra todas las imágenes de un motel.
     */
    Mono<Void> deleteByMotelId(Integer motelId);

    /**
     * Borra por rol (PROFILE o COVER típicamente).
     */
    @Query("DELETE FROM motel_images WHERE motel_id = :motelId AND role = :role")
    Mono<Integer> deleteByMotelIdAndRole(@Param("motelId") Integer motelId, @Param("role") String role);

    /**
     * Máximo order_index de galería.
     */
    @Query("""
        SELECT COALESCE(MAX(order_index), 0)
        FROM motel_images
        WHERE motel_id = :motelId AND role = 'GALLERY'
    """)
    Mono<Integer> maxGalleryOrder(@Param("motelId") Integer motelId);
}