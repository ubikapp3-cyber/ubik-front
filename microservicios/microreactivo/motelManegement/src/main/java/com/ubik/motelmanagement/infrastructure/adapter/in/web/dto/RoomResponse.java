package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import java.util.List;

/**
 * DTO de respuesta para Room
 */
public record RoomResponse(
        Long id,
        Long motelId,
        String number,
        String roomType,
        Double price,
        String description,
        Boolean isAvailable,
        List<String> imageUrls
) {
}