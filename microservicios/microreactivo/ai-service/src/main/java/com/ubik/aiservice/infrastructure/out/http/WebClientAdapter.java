package com.ubik.aiservice.infrastructure.out.http;

import com.ubik.aiservice.domain.port.out.ExternalServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebClientAdapter implements ExternalServicePort {

    private final WebClient webClient;

    @Override
    public Mono<List<Map<String, Object>>> getMyMotels(String token) {

        String formattedToken = token;

        if (token != null && !token.startsWith("Bearer ")) {
            formattedToken = "Bearer " + token;
        }

        return webClient.get()
                .uri("http://localhost:8080/api/auth/motels/my-motels") // 🔥 USA EL GATEWAY
                .header("Authorization", formattedToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    @Override
    public Mono<List<Map<String, Object>>> getPublicMotels() {
        return webClient.get()
                .uri("http://localhost:8083/api/motels")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }
}
