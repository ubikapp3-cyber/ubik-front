package com.ubik.paymentservice.domain.port.out;

import reactor.core.publisher.Mono;

public interface MotelMpPort {
    Mono<String> getAccessToken(Long motelId);
}
