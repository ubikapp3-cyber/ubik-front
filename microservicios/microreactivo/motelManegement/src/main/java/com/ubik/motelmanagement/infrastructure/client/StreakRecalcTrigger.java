package com.ubik.motelmanagement.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StreakRecalcTrigger {

    private final WebClient webClient;

    public StreakRecalcTrigger(
            @Value("${services.streak.url:http://streak-service:8086}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<Void> triggerRecalculate(Long userId) {
        return webClient.post()
                .uri("/api/streaks/{userId}/recalculate", userId)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(Void.class);
    }
}
