package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para rechazar un motel (solo admin)
 */
public record RejectMotelRequest(
        @NotBlank(message = "La razón del rechazo es requerida")
        @Size(min = 10, max = 1000, message = "La razón debe tener entre 10 y 1000 caracteres")
        String reason
) {
}