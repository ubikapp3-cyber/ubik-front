package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO para actualizar una Room existente
 */
public record UpdateRoomRequest(
        @NotBlank(message = "El número de habitación es requerido")
        @Size(max = 20, message = "El número de habitación no puede exceder 20 caracteres")
        String number,

        @NotBlank(message = "El tipo de habitación es requerido")
        @Size(max = 50, message = "El tipo de habitación no puede exceder 50 caracteres")
        String roomType,

        @NotNull(message = "El precio es requerido")
        @Positive(message = "El precio debe ser mayor que cero")
        Double price,

        @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
        String description,

        @NotNull(message = "El estado de disponibilidad es requerido")
        Boolean isAvailable,

        @Size(max = 15, message = "No se pueden agregar más de 15 imágenes")
        List<@Size(max = 500, message = "La URL de la imagen no puede exceder 500 caracteres") String> imageUrls
) {
}