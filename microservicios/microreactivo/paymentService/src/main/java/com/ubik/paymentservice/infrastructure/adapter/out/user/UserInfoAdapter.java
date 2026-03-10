package com.ubik.paymentservice.infrastructure.adapter.out.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserInfoAdapter {

    private static final Logger log = LoggerFactory.getLogger(UserInfoAdapter.class);
    private final WebClient webClient;

    public UserInfoAdapter(@Value("${services.user-management.url:http://usermanagement-service:8081}") String userManagementUrl) {
         this.webClient = WebClient.builder()
                 .baseUrl(userManagementUrl)
                 .build();
    }

    public Mono<UserProfileDto> getUserInfo(Long userId) {
        log.info("Obteniendo información del usuario {}", userId);

        return webClient.get()
                .uri("/api/user/{id}", userId)
                .retrieve()
                .bodyToMono(UserProfileDto.class)
                .doOnError(e -> log.error("Error obteniendo usuario {}: {}", userId, e.getMessage()));
    }

    public record UserProfileDto(String username, String email, String phoneNumber) {}
}
