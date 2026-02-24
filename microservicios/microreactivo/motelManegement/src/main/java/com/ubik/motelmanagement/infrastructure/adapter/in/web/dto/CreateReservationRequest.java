package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * DTO para crear una nueva Reservation
 * NO incluye confirmationCode porque se genera automáticamente
 */
public record CreateReservationRequest(
        @NotNull(message = "El ID de la habitación es requerido")
        Long roomId,

        @NotNull(message = "El ID del usuario es requerido")
        Long userId,

        @NotNull(message = "La fecha de check-in es requerida")
        @Future(message = "La fecha de check-in debe ser en el futuro")
        LocalDateTime checkInDate,

        @NotNull(message = "La fecha de check-out es requerida")
        @Future(message = "La fecha de check-out debe ser en el futuro")
        LocalDateTime checkOutDate,

        @NotNull(message = "El precio total es requerido")
        @Positive(message = "El precio total debe ser mayor que cero")
        Double totalPrice,

        @Size(max = 500, message = "Las solicitudes especiales no pueden exceder 500 caracteres")
        String specialRequests

) {
}