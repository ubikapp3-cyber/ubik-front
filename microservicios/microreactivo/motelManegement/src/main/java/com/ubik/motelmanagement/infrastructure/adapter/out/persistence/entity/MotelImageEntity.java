package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad de persistencia para imágenes de moteles
 */
@Table("motel_images")
public record MotelImageEntity(
        @Id Integer id,
        Integer motelId,
        String imageUrl,
        Integer orderIndex
) {
}
