package com.ubik.paymentservice.infrastructure.adapter.out.usermanagement;

import com.ubik.paymentservice.domain.port.out.UserManagementPort;
import com.ubik.paymentservice.infrastructure.adapter.out.usermanagement.dto.UserProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserManagementAdapter implements UserManagementPort {

    private static final Logger log = LoggerFactory.getLogger(UserManagementAdapter.class);

    private final WebClient webClient;

    public UserManagementAdapter(
            @Value("${services.user-management.url}") String userManagementUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(userManagementUrl)
                .build();
    }

    @Override
    public Mono<UserProfileDto> getUserProfile(Long userId) {
        log.info("Obteniendo perfil del usuario {} desde user-management", userId);

        return webClient.get()
                .uri("/api/user/{id}", userId)
                .retrieve()
                .bodyToMono(UserProfileDto.class)
                .doOnError(e -> log.error("Error obteniendo perfil del usuario {}: {}", userId, e.getMessage()));
    }
}
