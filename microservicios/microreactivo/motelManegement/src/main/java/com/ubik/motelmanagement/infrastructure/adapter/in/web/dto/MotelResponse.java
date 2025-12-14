package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para Motel
 */
public record MotelResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String description,
        String city,
        Long propertyId,
        LocalDateTime dateCreated,
        List<String> imageUrls
) {
}