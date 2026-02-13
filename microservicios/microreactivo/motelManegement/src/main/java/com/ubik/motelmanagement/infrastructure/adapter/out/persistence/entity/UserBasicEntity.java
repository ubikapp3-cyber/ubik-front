package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad básica de User (solo para leer id y username)
 * No incluye todos los campos, solo lo necesario
 */
@Table("users")
public record UserBasicEntity(
        @Id Long id,
        String username,
        String email,
        @Column("role_id") Long roleId
) {
}