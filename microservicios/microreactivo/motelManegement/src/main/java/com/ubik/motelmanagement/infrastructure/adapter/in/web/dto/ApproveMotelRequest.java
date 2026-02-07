package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
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

/**
 * DTO para rechazar un motel (solo admin)
 */
public record RejectMotelRequest(
        @NotBlank(message = "La razón del rechazo es requerida")
        @Size(min = 10, max = 1000, message = "La razón debe tener entre 10 y 1000 caracteres")
        String reason
) {
}

/**
 * DTO para respuesta de operación de aprobación
 */
public record ApprovalOperationResponse(
        Long motelId,
        String motelName,
        String previousStatus,
        String newStatus,
        String message,
        java.time.LocalDateTime operationDate,
        Long performedByUserId
) {
}
