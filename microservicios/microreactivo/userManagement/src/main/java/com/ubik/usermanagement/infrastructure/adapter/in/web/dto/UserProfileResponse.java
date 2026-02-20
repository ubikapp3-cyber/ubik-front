package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para perfil de usuario
 */
public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        boolean anonymous,
        Long roleId,
        BigDecimal longitude,
        BigDecimal latitude,
        LocalDate birthDate,
        Integer age 
) {
}