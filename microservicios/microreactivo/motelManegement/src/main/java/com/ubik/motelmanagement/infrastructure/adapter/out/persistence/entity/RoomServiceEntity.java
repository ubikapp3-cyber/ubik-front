package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad de persistencia para la tabla intermedia room_service
 * Representa la relación muchos-a-muchos entre Room y Service
 */
@Table("room_service")
public record RoomServiceEntity(
        @Column("room_id") Long roomId,
        @Column("service_id") Long serviceId
) {
}
