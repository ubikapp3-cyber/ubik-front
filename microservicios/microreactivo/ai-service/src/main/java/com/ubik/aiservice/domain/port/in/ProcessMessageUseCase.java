package com.ubik.aiservice.domain.port.in;

import com.ubik.aiservice.infrastructure.in.web.dto.AiResponse;
import reactor.core.publisher.Mono;

public interface ProcessMessageUseCase {

    Mono<AiResponse> process(String role, String message, String token);
}