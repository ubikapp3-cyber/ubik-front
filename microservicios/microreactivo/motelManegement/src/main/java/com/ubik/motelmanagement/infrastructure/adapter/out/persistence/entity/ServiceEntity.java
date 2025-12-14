package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para Service
 * Representa la tabla 'service' en la base de datos
 */
@Table("service")
public record ServiceEntity(
        @Id Long id,
        String name,
        String description,
        String icon,
        @Column("created_at") LocalDateTime createdAt
) {
}
