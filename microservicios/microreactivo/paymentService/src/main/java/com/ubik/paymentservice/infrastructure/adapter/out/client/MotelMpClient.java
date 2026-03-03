package com.ubik.paymentservice.infrastructure.adapter.out.client;

import com.ubik.paymentservice.domain.port.out.MotelMpPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MotelMpClient implements MotelMpPort {

    private static final Logger log = LoggerFactory.getLogger(MotelMpClient.class);
    private final WebClient webClient;

    public MotelMpClient(
            @Value("${services.motel-management.url}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<String> getAccessToken(Long motelId) {
        log.info("Pidiendo access_token motelId={}", motelId);
        return webClient.get()
                .uri("/api/motels/oauth/token/{id}", motelId)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("Error obteniendo token para motel={}: {}", motelId, e.getMessage()));
    }
}
