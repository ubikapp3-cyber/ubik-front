package com.ubik.motelmanagement.domain.model;

import java.time.LocalDate;

/**
 * DTO para el ingreso diario en la gráfica semanal
 */
public record WeeklyRevenue(
        LocalDate day,
        Double revenue
) {}
