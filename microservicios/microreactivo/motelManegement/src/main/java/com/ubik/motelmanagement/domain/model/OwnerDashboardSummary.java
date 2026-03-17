package com.ubik.motelmanagement.domain.model;

import java.util.Map;

/**
 * DTO para el resumen del dashboard del propietario
 */
public record OwnerDashboardSummary(
        Map<String, Long> reservationsByStatus,
        Double dailyRevenue,
        Double occupancyRate,
        Long totalRooms,
        Long activeReservations
) {}
