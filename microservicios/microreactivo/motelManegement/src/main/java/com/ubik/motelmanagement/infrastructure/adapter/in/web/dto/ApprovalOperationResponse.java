package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de operación de aprobación
 */
public record ApprovalOperationResponse(
        Long motelId,
        String motelName,
        String previousStatus,
        String newStatus,
        String message,
        LocalDateTime operationDate,
        Long performedByUserId
) {
}