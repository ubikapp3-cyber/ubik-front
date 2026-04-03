package com.ubik.streakservice.domain.port.in.dto;

public record OverrideStreakRequest(
    String level, // NEW, AMATEUR, GOLD
    String overrideReason
) {}
