package com.ubik.streakservice.domain.model;

public record UserSummary(
    Long id,
    String username,
    String email
) {}
