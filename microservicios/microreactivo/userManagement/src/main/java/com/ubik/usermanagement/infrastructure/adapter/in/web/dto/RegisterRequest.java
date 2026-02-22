package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registro de nuevos usuarios
 */
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required")
        String email,

        // ✅ AGREGADO: Campo phoneNumber
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,

        @NotNull(message = "Anonymous flag is required")
        Boolean anonymous,

        @NotNull(message = "Role ID is required")
        @JsonDeserialize(using = LongFromStringDeserializer.class)
        Long roleId,

        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        BigDecimal longitude,

        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        BigDecimal latitude,

        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {}