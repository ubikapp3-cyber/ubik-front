package com.ubik.aiservice.infrastructure.out.http;

import com.ubik.aiservice.domain.port.out.ExternalServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;

    @Override
    public Mono<List<Map<String, Object>>> getMyMotels(String token) {

        String formattedToken = token;

        if (token != null && !token.startsWith("Bearer ")) {
            formattedToken = "Bearer " + token;
        }

        return webClient.get()
                .uri(gatewayBaseUrl + "/api/auth/motels/my-motels")
                .header("Authorization", formattedToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    @Override
    public Mono<List<Map<String, Object>>> getPublicMotels() {
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/motels")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    @Override
    public Mono<Map<String, Object>> getMyProfile(String token) {
        String formattedToken = token;

        if (token != null && !token.startsWith("Bearer ")) {
            formattedToken = "Bearer " + token;
        }

        return webClient.get()
                .uri(gatewayBaseUrl + "/api/user")
                .header("Authorization", formattedToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    @Override
    public Mono<List<Map<String, Object>>> getMyReservations(String token, String userId) {
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/reservations/user/" + userId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    @Override
    public Mono<List<Map<String, Object>>> getRoomsByMotel(Long motelId) {
        return webClient.get()
                .uri(gatewayBaseUrl + "/api/rooms/motel/" + motelId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }
}
