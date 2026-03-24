package com.ubik.aiservice.infrastructure.out.ollama;

import com.ubik.aiservice.domain.port.out.LlmClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OllamaClientAdapter implements LlmClientPort {

    private final WebClient webClient;

    @Override
    public Mono<String> generate(String prompt) {

        Map<String, Object> request = Map.of(
                "model", "llama3",
                "prompt", prompt,
                "stream", false
        );

        return webClient.post()
                .uri("http://localhost:11434/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> (String) response.get("response"));
    }
}