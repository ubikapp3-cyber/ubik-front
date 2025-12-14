package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para Motel
 * Representa la tabla en la base de datos
 */
@Table("motel")
public record MotelEntity(
        @Id Long id,
        String name,
        String address,
        @Column("phone_number") String phoneNumber,
        String description,
        String city,
        @Column("property_id") Long propertyId,
        @Column("date_created") LocalDateTime dateCreated
) {
}