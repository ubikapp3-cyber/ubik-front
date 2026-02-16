package com.ubik.usermanagement.infrastructure.adapter.out.notificationAdapter;

import com.ubik.usermanagement.application.port.out.NotificationPort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class NotificationAdapter implements NotificationPort {

    private final WebClient webClient;

    public NotificationAdapter(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://notification-service:8084")
                //.baseUrl("http://host.docker.internal:8084")
                .build();
    }

    @Override
    public Mono<Void> sendPasswordRecoveryEmail(String email, String token) {

        Map<String, String> body = Map.of(
                "to", email,
                "subject", "Recuperación de contraseña",
                "message", "Tu token de recuperación es: " + token
        );

        return webClient.post()
                .uri("/notifications/email")
                .header("X-Internal-Request", "true")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
