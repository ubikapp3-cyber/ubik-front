package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para crear un nuevo Service
 */
public record CreateServiceRequest(
        @NotBlank(message = "El nombre del servicio es requerido")
        @Size(max = 50, message = "El nombre del servicio no puede exceder 50 caracteres")
        String name,

        @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
        String description,

        @Size(max = 50, message = "El icono no puede exceder 50 caracteres")
        String icon
) {
}
