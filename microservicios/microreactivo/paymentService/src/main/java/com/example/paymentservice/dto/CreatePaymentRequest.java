package com.example.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(
        @NotNull(message = "reservationId es requerido")
        Long reservationId,

        @NotNull(message = "motelId es requerido")
        Long motelId,

        @NotNull @Positive(message = "amount debe ser mayor a 0")
        Double amount,

        String currency  // default "COP"
) {}