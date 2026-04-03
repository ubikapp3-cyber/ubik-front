package com.ubik.streakservice.domain.port.in.dto;

import java.time.LocalDateTime;

public record AdminUserStreakResponse(
    Long userId,
    String username,
    String email,
    String level,
    Integer reservationsLast30Days,
    Double discountRate,
    LocalDateTime calculatedAt,
    String overriddenLevel,
    String overrideReason,
    Long updatedBy
) {}
