package com.ubik.streakservice.domain.port.in.dto;

public record AdminStreakStatsResponse(
    long totalUsers,
    long newUsers,
    long amateurUsers,
    long goldUsers,
    double newPercentage,
    double amateurPercentage,
    double goldPercentage,
    Long topUserId,
    Integer topReservations
) {}
