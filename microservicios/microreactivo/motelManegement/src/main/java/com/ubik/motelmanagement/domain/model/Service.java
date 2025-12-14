package com.ubik.motelmanagement.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para Service (Servicio)
 * Representa los servicios disponibles en las habitaciones del motel
 */
public record Service(
        Long id,
        String name,
        String description,
        String icon,
        LocalDateTime createdAt
) {
    /**
     * Constructor para creación de nuevos servicios (sin ID ni timestamp)
     */
    public static Service createNew(
            String name,
            String description,
            String icon
    ) {
        return new Service(null, name, description, icon, null);
    }

    /**
     * Constructor para actualización
     */
    public Service withUpdatedInfo(
            String name,
            String description,
            String icon
    ) {
        return new Service(this.id, name, description, icon, this.createdAt);
    }
}
