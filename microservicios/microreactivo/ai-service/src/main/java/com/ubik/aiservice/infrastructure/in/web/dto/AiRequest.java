package com.ubik.aiservice.infrastructure.in.web.dto;

public record AiRequest(
        String role,
        String message,
        String token
) {}
