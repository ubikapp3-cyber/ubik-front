package com.ubik.streakservice.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StreakResponse(
        Long userId,
        String level,
        int reservationsLast30Days,
        double discountRate,
        List<String> benefits,
        LocalDateTime calculatedAt
) {}
