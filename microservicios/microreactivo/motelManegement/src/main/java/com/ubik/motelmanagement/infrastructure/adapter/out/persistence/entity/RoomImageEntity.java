package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad de persistencia para imágenes de habitaciones
 */
@Table("room_images")
public record RoomImageEntity(
        @Id Integer id,
        Integer roomId,
        String imageUrl,
        Integer orderIndex
) {
}
