package com.ubik.aiservice.infrastructure.in.web;

import com.ubik.aiservice.domain.port.in.ProcessMessageUseCase;
import com.ubik.aiservice.infrastructure.in.web.dto.AiRequest;
import com.ubik.aiservice.infrastructure.in.web.dto.AiResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final ProcessMessageUseCase useCase;

    @PostMapping
    public Mono<ResponseEntity<AiResponse>> process(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody AiRequest request
    ) {
        return useCase.process(request.role(), request.message(), token)
                .map(ResponseEntity::ok);
    }
}
