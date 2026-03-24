package com.ubik.aiservice.domain.port.out;

import reactor.core.publisher.Mono;

public interface LlmClientPort {

    Mono<String> generate(String prompt);
}
