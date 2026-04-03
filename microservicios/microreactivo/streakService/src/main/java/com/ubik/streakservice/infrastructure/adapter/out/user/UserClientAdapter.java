package com.ubik.streakservice.infrastructure.adapter.out.user;

import com.ubik.streakservice.domain.model.UserSummary;
import com.ubik.streakservice.domain.port.out.UserQueryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserClientAdapter implements UserQueryPort {

    private final WebClient webClient;

    public UserClientAdapter(
            @Value("${services.user-management.url:http://user-management-service:8081}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<UserSummary> getUserSummary(Long userId) {
        return webClient.get()
                .uri("/api/user/{id}", userId)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(UserSummary.class)
                .onErrorResume(e -> Mono.just(new UserSummary(userId, "Unknown", "unknown@ubik.com")));
    }
}
