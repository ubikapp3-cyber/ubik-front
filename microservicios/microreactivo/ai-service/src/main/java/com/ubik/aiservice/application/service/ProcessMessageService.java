package com.ubik.aiservice.application.service;

import com.ubik.aiservice.domain.model.Intent;
import com.ubik.aiservice.domain.port.in.ProcessMessageUseCase;
import com.ubik.aiservice.domain.port.out.ExternalServicePort;
import com.ubik.aiservice.domain.port.out.LlmClientPort;
import com.ubik.aiservice.infrastructure.in.web.dto.AiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ubik.aiservice.domain.model.Intent.GET_PUBLIC_MOTELS;
import static com.ubik.aiservice.domain.model.Intent.GET_MY_MOTELS;

@Service
@RequiredArgsConstructor
public class ProcessMessageService implements ProcessMessageUseCase {

    private final LlmClientPort llmClient;
    private final ExternalServicePort externalService;

    private String buildPrompt(String message) {
        return """
        Eres un sistema que clasifica intenciones.

        Devuelve SOLO un JSON válido:
        {
          "intent": "NOMBRE_INTENCION"
        }

        Intenciones:
                - GET_MY_MOTELS
                - GET_PUBLIC_MOTELS
                - GENERAL_INFO

        Mensaje:
        "%s"
        """.formatted(message);
    }

    private Intent parseIntent(String raw) {
        try {
            String cleaned = raw.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            if (cleaned.contains("GET_MY_MOTELS")) return Intent.GET_MY_MOTELS;
            if (cleaned.contains("GET_PUBLIC_MOTELS")) return Intent.GET_PUBLIC_MOTELS;

            return Intent.GENERAL_INFO;

        } catch (Exception e) {
            return Intent.UNKNOWN;
        }
    }

    @Override
    public Mono<AiResponse> process(String role, String message, String token) {

        String prompt = buildPrompt(message);

        return llmClient.generate(prompt) // Mono<String>
                .map(this::parseIntent)   // Mono<Intent>
                .flatMap(intent -> {

                    // 🔐 VALIDACIÓN DE AUTENTICACIÓN
                    if (intent.requiresAuth() && (token == null || token.isBlank())) {
                        return Mono.just(new AiResponse(
                                intent.name(),
                                "Debes iniciar sesión para realizar esta acción",
                                null
                        ));
                    }

                    return switch (intent) {

                        case GET_MY_MOTELS ->
                                externalService.getMyMotels(token)
                                        .map(this::simplifyMotels)
                                        .map(data -> new AiResponse(
                                                intent.name(),
                                                "Estos son tus moteles registrados",
                                                data
                                        ));

                        case GET_PUBLIC_MOTELS ->
                                externalService.getPublicMotels()
                                        .map(this::simplifyMotels)
                                        .map(data -> new AiResponse(
                                                intent.name(),
                                                "Estos son los moteles disponibles",
                                                data
                                        ));

                        default ->
                                Mono.just(new AiResponse(
                                        "GENERAL_INFO",
                                        "No entendí completamente",
                                        null
                                ));
                    };
                });
    }
    private List<Map<String, Object>> simplifyMotels(List<Map<String, Object>> motels) {

        return motels.stream().map(motel -> {

            Map<String, Object> simplified = new HashMap<>();

            simplified.put("id", motel.get("id"));
            simplified.put("name", motel.get("name"));
            simplified.put("city", motel.get("city"));
            simplified.put("address", motel.get("address"));
            simplified.put("description", motel.get("description"));

            // 🖼 Imagen principal (primera del array)
            List<Map<String, Object>> images = (List<Map<String, Object>>) motel.get("imageUrls");

            if (images != null && !images.isEmpty()) {
                simplified.put("image", images.get(0).get("url"));
            }

            return simplified;

        }).toList();
    }
}