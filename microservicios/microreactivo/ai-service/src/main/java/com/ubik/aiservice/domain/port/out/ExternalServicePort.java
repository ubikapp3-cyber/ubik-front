package com.ubik.aiservice.domain.port.out;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ExternalServicePort {

    Mono<List<Map<String, Object>>> getMyMotels(String token);

    Mono<List<Map<String, Object>>> getPublicMotels();

}