package com.ubik.motelmanagement.infrastructure.adapter.out.WebClient;


import com.ubik.motelmanagement.domain.model.UserSummary;
import com.ubik.motelmanagement.domain.port.out.UserPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserClientAdapter implements UserPort {

    private final WebClient webClient;

    public UserClientAdapter(
            @Value("${services.user-management.url}") String baseUrl,
            WebClient.Builder builder
    ) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public Mono<UserSummary> getUserById(Long id) {

        return webClient.get()
                .uri("/api/user/{id}", id)
                .header("X-Internal-Request", "true")
                .retrieve()
                .bodyToMono(UserSummary.class);
    }
}
