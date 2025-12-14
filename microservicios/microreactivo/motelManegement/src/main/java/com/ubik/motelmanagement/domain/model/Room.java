package com.ubik.motelmanagement.domain.model;

import java.util.List;

/**
 * Modelo de dominio para Room (Habitación)
 * Representa la entidad de negocio independiente de la infraestructura
 */
public record Room(
        Long id,
        Long motelId,
        String number,
        String roomType,
        Double price,
        String description,
        Boolean isAvailable,
        List<String> imageUrls  // Nueva propiedad para URLs de imágenes
) {
    // Constructor para creación de nuevas habitaciones (sin ID)
    public static Room createNew(
            Long motelId,
            String number,
            String roomType,
            Double price,
            String description,
            List<String> imageUrls
    ) {
        return new Room(null, motelId, number, roomType, price, description, true, imageUrls);
    }

    // Constructor para actualización
    public Room withUpdatedInfo(
            String number,
            String roomType,
            Double price,
            String description,
            Boolean isAvailable,
            List<String> imageUrls
    ) {
        return new Room(this.id, this.motelId, number, roomType, price, description, isAvailable, imageUrls);
    }
}