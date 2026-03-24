package com.ubik.aiservice.infrastructure.in.web.dto;

import java.util.List;
import java.util.Map;

public record AiResponse(
        String intent,
        String message,
        List<Map<String, Object>> data
) {}
