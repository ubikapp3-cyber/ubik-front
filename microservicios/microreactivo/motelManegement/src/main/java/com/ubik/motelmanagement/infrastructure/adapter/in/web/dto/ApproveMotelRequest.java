package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO para aprobar un motel (solo admin)
 */
public record ApproveMotelRequest(
        // Opcional: comentarios del admin sobre la aprobación
        @Size(max = 500, message = "Los comentarios no pueden exceder 500 caracteres")
        String comments
) {
}