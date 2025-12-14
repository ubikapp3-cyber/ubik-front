package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para Service
 */
public record ServiceResponse(
        Long id,
        String name,
        String description,
        String icon,
        LocalDateTime createdAt
) {
}
