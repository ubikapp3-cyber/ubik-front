package com.ubik.paymentservice.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull Long reservationId,
        @NotNull Long motelId,
        @NotNull @Positive BigDecimal amount,
        String currency
) {}
