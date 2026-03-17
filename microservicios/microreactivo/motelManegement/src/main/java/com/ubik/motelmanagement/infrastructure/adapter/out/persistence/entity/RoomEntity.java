package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad de persistencia para Room
 * Representa la tabla 'room' en la base de datos
 */
@Table("room")
public record RoomEntity(
        @Id Long id,
        @Column("motel_id") Long motelId,
        String number,
        @Column("room_type") String roomType,
        Double price,
        String description,
        @Column("is_available") Boolean isAvailable,
        @Column("latitude") Double latitude,
        @Column("longitude") Double longitude
) {
}