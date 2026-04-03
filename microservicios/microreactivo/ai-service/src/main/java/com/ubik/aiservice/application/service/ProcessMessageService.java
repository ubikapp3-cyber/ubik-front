package com.ubik.aiservice.application.service;

import com.ubik.aiservice.domain.model.Intent;
import com.ubik.aiservice.domain.port.in.ProcessMessageUseCase;
import com.ubik.aiservice.domain.port.out.ExternalServicePort;
import com.ubik.aiservice.domain.port.out.LlmClientPort;
import com.ubik.aiservice.infrastructure.in.web.dto.AiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ubik.aiservice.domain.model.Intent.GET_PUBLIC_MOTELS;
import static com.ubik.aiservice.domain.model.Intent.GET_MY_MOTELS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessMessageService implements ProcessMessageUseCase {

    private final LlmClientPort llmClient;
    private final ExternalServicePort externalService;

    /*private String buildPrompt(String message) {
        return """
        Eres un sistema que clasifica intenciones.

        Devuelve SOLO un JSON válido:
        {
          "intent": "NOMBRE_INTENCION"
        }

        Intenciones:
                - GET_MY_PROFILE
                - GET_MY_MOTELS
                - GET_MY_RESERVATIONS
                - GET_PUBLIC_MOTELS
                - GET_ROOMS_BY_MOTEL
                - TUTORIAL_LOGIN
                - TUTORIAL_REGISTER
                - TUTORIAL_REGISTER_MOTEL
                - TUTORIAL_REGISTER_ROOM
                - GENERAL_INFO
                - UNKNOWN

        Mensaje:
        "%s"
        """.formatted(message);
    }*/
    private String buildPrompt(String message) {
        return """
                Eres un sistema que clasifica intenciones de usuarios en una aplicación de moteles.
                
                Devuelve SOLO un JSON válido:
                { "intent": "NOMBRE_INTENCION" }
                
                REGLAS:
                - Solo una intención
                - Sin explicaciones
                - Si no entiendes → UNKNOWN
                
                INTENCIONES:
                
                GET_MY_PROFILE → ver perfil
                GET_MY_MOTELS → ver mis moteles
                GET_MY_RESERVATIONS → ver mis reservas
                GET_PUBLIC_MOTELS → explorar moteles
                GET_ROOMS_BY_MOTEL → ver habitaciones de un motel
                
                TUTORIAL_LOGIN → ayuda para iniciar sesión
                TUTORIAL_REGISTER → crear cuenta (registrarme, signup)
                
                TUTORIAL_REGISTER_MOTEL → registrar motel
                TUTORIAL_REGISTER_ROOM → registrar habitación (cuarto, room)
                
                GENERAL_INFO → información general
                UNKNOWN → no entendido
                
                REGLAS CLAVE:
                - "registrar" solo → TUTORIAL_REGISTER
                - "registrar motel" → TUTORIAL_REGISTER_MOTEL
                - "registrar habitación/cuarto/room" → TUTORIAL_REGISTER_ROOM
                
                EJEMPLOS:
                
                "quiero registrarme" → TUTORIAL_REGISTER
                "registrar motel" → TUTORIAL_REGISTER_MOTEL
                "registrar habitacion" → TUTORIAL_REGISTER_ROOM
                "ver mis reservas" → GET_MY_RESERVATIONS
                "hola" → GENERAL_INFO
                "asdfgh" → UNKNOWN
                
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
            if (cleaned.contains("GET_MY_PROFILE")) return Intent.GET_MY_PROFILE;
            if (cleaned.contains("GET_MY_RESERVATIONS")) return Intent.GET_MY_RESERVATIONS;
            if (cleaned.contains("GET_ROOMS_BY_MOTEL")) return Intent.GET_ROOMS_BY_MOTEL;
            if (cleaned.contains("TUTORIAL_LOGIN")) return Intent.TUTORIAL_LOGIN;
            if (cleaned.contains("TUTORIAL_REGISTER_MOTEL")) return Intent.TUTORIAL_REGISTER_MOTEL;
            if (cleaned.contains("TUTORIAL_REGISTER_ROOM")) return Intent.TUTORIAL_REGISTER_ROOM;
            if (cleaned.contains("TUTORIAL_REGISTER")) return Intent.TUTORIAL_REGISTER;

            if (cleaned.contains("LOADING")) return Intent.LOADING;

            return Intent.GENERAL_INFO;

        } catch (Exception e) {
            return Intent.UNKNOWN;
        }
    }
    private AtomicBoolean modelReady = new AtomicBoolean(false);
    @PostConstruct
    public void warmUpModel() {
        log.info("🔥 Warming up LLM model...");

        llmClient.generate("ping")
                .doOnNext(res -> {
                    modelReady.set(true);
                    log.info("✅ LLM warm-up completed");
                })
                .doOnError(err -> log.error("❌ LLM warm-up failed", err))
                .subscribe();
    }
    @Override
    public Mono<AiResponse> process(String role, String message, String token) {

        log.info("📩 Incoming request | role={} | message='{}'", role, message);

        String prompt = buildPrompt(message);
        long start = System.currentTimeMillis();

        if (!modelReady.get()) {
            log.warn("⏳ LLM aún cargando...");

            return Mono.just(new AiResponse(
                    "LOADING",
                    "Estoy iniciando el motor de IA, intenta nuevamente en unos segundos 🚀",
                    null
            ));
        }

        return llmClient.generate(prompt)

                // ⏱️ medir tiempo
                .doOnNext(res -> {
                    long duration = System.currentTimeMillis() - start;
                    log.info("⏱️ LLM response time: {} ms", duration);
                })

                // 🔹 RAW antes de cualquier transformación
                .doOnNext(res -> log.debug("🤖 LLM RAW RESPONSE: {}", res))

                // 🧯 timeout
                .timeout(Duration.ofSeconds(20))

                // 🔥 manejo específico
//                .onErrorResume(TimeoutException.class, e -> {
//                    log.error("⏰ LLM TIMEOUT", e);
//                    return Mono.just("{\"intent\":\"UNKNOWN\"}");
//                })
                .onErrorResume(TimeoutException.class, e -> {
                    log.error("⏰ LLM TIMEOUT - probablemente modelo frío");

                    return Mono.just("{\"intent\":\"LOADING\"}");
                })

                .onErrorResume(e -> {
                    log.error("❌ LLM ERROR", e);
                    return Mono.just("{\"intent\":\"UNKNOWN\"}");
                })

                // 🔹 parseo
                .map(this::parseIntent)

                // 🔹 log intent
                .doOnNext(intent -> log.info("🎯 Detected intent: {}", intent))

                .flatMap(intent -> {

                    // 🔐 VALIDACIÓN DE AUTENTICACIÓN
                    if (intent.requiresAuth() && (token == null || token.isBlank())) {
                        log.warn("🔒 Unauthorized access attempt | intent={}", intent);

                        return Mono.just(new AiResponse(
                                intent.name(),
                                "Debes iniciar sesión para realizar esta acción",
                                null
                        ));
                    }

                    log.debug("➡️ Executing flow for intent={}", intent);

                    return switch (intent) {

                        case GET_MY_PROFILE -> {
                            log.info("👤 Fetching user profile");

                            yield externalService.getMyProfile(token)
                                    .map(this::simplifyProfile)
                                    .map(data -> List.of(data))
                                    .map(data -> new AiResponse(
                                            intent.name(),
                                            "Este es tu perfil",
                                            data
                                    ))
                                    .doOnError(e -> log.error("❌ Error fetching profile", e));
                        }

                        case GET_MY_RESERVATIONS -> {
                            String userId = extractUserId(token);

                            log.info("📅 Fetching reservations | userId={}", userId);

                            yield externalService.getMyReservations(userId, token)
                                    .map(this::simplifyReservations)
                                    .map(data -> new AiResponse(
                                            intent.name(),
                                            "Estas son tus reservas",
                                            data
                                    ))
                                    .doOnError(e -> log.error("❌ Error fetching reservations", e));
                        }

                        case GET_MY_MOTELS -> {
                            log.info("🏨 Fetching user motels");

                            yield externalService.getMyMotels(token)
                                    .map(this::simplifyMotels)
                                    .map(data -> new AiResponse(
                                            intent.name(),
                                            "Estos son tus moteles registrados",
                                            data
                                    ))
                                    .doOnError(e -> log.error("❌ Error fetching motels", e));
                        }

                        case GET_PUBLIC_MOTELS -> {
                            log.info("🌍 Fetching public motels");

                            yield externalService.getPublicMotels()
                                    .map(this::simplifyMotels)
                                    .map(data -> new AiResponse(
                                            intent.name(),
                                            "Estos son los moteles disponibles",
                                            data
                                    ))
                                    .doOnError(e -> log.error("❌ Error fetching public motels", e));
                        }

                        case TUTORIAL_LOGIN -> {
                            log.info("📘 Tutorial: LOGIN");
                            yield Mono.just(new AiResponse(intent.name(), getLoginTutorial(), null));
                        }

                        case TUTORIAL_REGISTER -> {
                            log.info("📘 Tutorial: REGISTER USER");
                            yield Mono.just(new AiResponse(intent.name(), getRegisterTutorial(), null));
                        }

                        case TUTORIAL_REGISTER_MOTEL -> {
                            log.info("📘 Tutorial: REGISTER MOTEL");
                            yield Mono.just(new AiResponse(intent.name(), getRegisterMotelTutorial(), null));
                        }

                        case TUTORIAL_REGISTER_ROOM -> {
                            log.info("📘 Tutorial: REGISTER ROOM");
                            yield Mono.just(new AiResponse(intent.name(), getRegisterRoomTutorial(), null));
                        }

                        case GENERAL_INFO -> {
                            log.info("ℹ️ General info requested");
                            yield Mono.just(new AiResponse(intent.name(), getGeneralInfo(), null));
                        }

                        case UNKNOWN -> {
                            log.warn("❓ Unknown intent detected");
                            yield Mono.just(new AiResponse(
                                    intent.name(),
                                    "No entendí muy bien tu solicitud. Puedes preguntarme por moteles, habitaciones o cómo usar la plataforma.",
                                    null
                            ));
                        }

                        case LOADING -> Mono.just(new AiResponse(
                                intent.name(),
                                "Estoy iniciando el motor de IA, intenta nuevamente en unos segundos 🚀",
                                null
                        ));

                        case GET_ROOMS_BY_MOTEL -> {
                            String motelName = extractMotelName(message);

                            log.info("🔍 Searching rooms | motelName='{}'", motelName);

                            yield findMotelByName(motelName)
                                    .flatMap(result -> {
                                        Long motelId = (Long) result;

                                        log.info("🏨 Motel found | id={}", motelId);

                                        return externalService.getRoomsByMotel(motelId)
                                                .map(this::simplifyRooms)
                                                .map(data -> new AiResponse(
                                                        intent.name(),
                                                        "Estas son las habitaciones disponibles",
                                                        data
                                                ));
                                    })
                                    .doOnError(e -> log.error("❌ Error fetching rooms", e))
                                    .onErrorResume(error -> {

                                        String msg = error.getMessage();

                                        if ("NOT_FOUND".equals(msg)) {
                                            log.warn("⚠️ Motel not found: {}", motelName);
                                            return Mono.just(new AiResponse(intent.name(),
                                                    "No encontré ese motel, intenta con otro nombre", null));
                                        }

                                        if (msg.startsWith("MULTIPLE:")) {
                                            log.warn("⚠️ Multiple motels found: {}", msg);
                                            String nombres = msg.replace("MULTIPLE:", "");

                                            return Mono.just(new AiResponse(intent.name(),
                                                    "Para tu búsqueda encontré varios moteles: " + nombres +
                                                            ". Por favor especifica cuál deseas",
                                                    null));
                                        }

                                        log.error("❌ Unexpected error", error);

                                        return Mono.just(new AiResponse(intent.name(),
                                                "Ocurrió un error al buscar las habitaciones",
                                                null));
                                    });
                        }

                        default -> {
                            log.warn("⚠️ Default fallback triggered");
                            yield Mono.just(new AiResponse(
                                    "GENERAL_INFO",
                                    "No entendí completamente",
                                    null
                            ));
                        }
                    };
                });
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "null";
        return token.substring(0, 5) + "..." + token.substring(token.length() - 5);
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

    private Map<String, Object> simplifyProfile(Map<String, Object> user) {

        Map<String, Object> simplified = new HashMap<>();

        simplified.put("username", user.get("username"));
        simplified.put("email", user.get("email"));
        simplified.put("phone", user.get("phoneNumber"));
        simplified.put("age", user.get("age"));

        return simplified;
    }

    private List<Map<String, Object>> simplifyReservations(List<Map<String, Object>> list) {

        return list.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();

            m.put("id", r.get("id"));
            m.put("roomId", r.get("roomId"));
            m.put("checkIn", r.get("checkInDate"));
            m.put("checkOut", r.get("checkOutDate"));
            m.put("status", r.get("status"));
            m.put("total", r.get("totalPrice"));

            return m;
        }).toList();
    }

    private List<Map<String, Object>> simplifyRooms(List<Map<String, Object>> rooms) {

        return rooms.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();

            m.put("id", r.get("id"));
            m.put("number", r.get("number"));
            m.put("type", r.get("roomType"));
            m.put("price", r.get("price"));
            m.put("available", r.get("isAvailable"));

            List<String> images = (List<String>) r.get("imageUrls");
            if (images != null && !images.isEmpty()) {
                m.put("image", images.get(0));
            }

            return m;
        }).toList();
    }

    private String extractMotelName(String message) {
        return message.toLowerCase()
                .replace("quiero ver", "")
                .replace("muestrame", "")
                .replace("ver", "")
                .replace("que", "")
                .replace("cuales", "")
                .replace("cuartos", "")
                .replace("habitaciones", "")
                .replace("dispone", "")
                .replace("tiene", "")
                .replace("el", "")
                .replace("del", "")
                .replace("de", "")
                .replace("motel", "")
                .trim();
    }

    private Mono<Long> findMotelIdByName(String name) {
        return externalService.getPublicMotels()
                .flatMapMany(Flux::fromIterable)
                .filter(m -> m.get("name").toString().toLowerCase().contains(name))
                .next()
                .map(m -> Long.valueOf(m.get("id").toString()));
    }

    private Mono<Object> findMotelByName(String name) {

        return externalService.getPublicMotels()
                .flatMapMany(Flux::fromIterable)
                .filter(m -> {
                    String motelName = m.get("name").toString().toLowerCase();
                    String search = name.toLowerCase();

                    List<String> keywords = Arrays.stream(search.split(" "))
                            .filter(word -> !STOP_WORDS.contains(word))
                            .toList();

                    return keywords.stream()
                            .allMatch(word -> motelName.contains(word));
                })
                .collectList()
                .flatMap(list -> {

                    if (list.isEmpty()) {
                        return Mono.error(new RuntimeException("NOT_FOUND"));
                    }

                    if (list.size() > 1) {

                        String nombres = list.stream()
                                .map(m -> m.get("name").toString())
                                .collect(Collectors.joining(", "));

                        return Mono.error(new RuntimeException("MULTIPLE:" + nombres));
                    }

                    Long motelId = Long.valueOf(list.get(0).get("id").toString());
                    return Mono.just(motelId);
                });
    }

    private String extractUserId(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "");

            String[] parts = cleanToken.split("\\.");
            String payload = new String(Base64.getDecoder().decode(parts[1]));

            return payload.split("\"userId\":")[1].split(",")[0];

        } catch (Exception e) {
            return "0";
        }
    }

    private static final Set<String> STOP_WORDS = Set.of(
            "quiero", "ver", "las", "los", "del", "de", "el",
            "la", "habitaciones", "cuartos", "motel", "que",
            "cuales", "tiene", "dispone"
    );

    private String getLoginTutorial() {
        return """
                Para iniciar sesión en UBIK:
                
                1. Accede a la plataforma
                2. Haz clic en "Inicia Sesión"
                3. Ingresa tu username y contraseña
                4. Presiona "Ingresar"
                
                Si no tienes cuenta, puedes registrarte fácilmente.
                """;
    }

    private String getRegisterTutorial() {
        return """
                Para registrarte en UBIK:
                
                1. Haz clic en "Registrarse"
                2. Elige el tipo de usuario:
                   - Cliente
                   - Propietario de establecimiento
                3. Completa el formulario
                4. Confirma tu cuenta desde el correo electrónico
                
                Una vez registrado podrás acceder a todas las funcionalidades.
                """;
    }

    private String getRegisterMotelTutorial() {
        return """
                Para registrar un motel en UBIK:
                
                1. Inicia sesion con tu cuenta (Propietario)
                2. En el dashboard operacional, selecciona la pestaña moteles
                3. Clic en agregar nuevo motel
                4. Completa el formularo y confirma la creacion (El motel quedara en espera de aprovacion por parte de nuestro equipo de Ubik)
                
                Una vez nuestro equivo verifique y confirme la creacion, sera visible para el publico.
                """;
    }

    private String getRegisterRoomTutorial() {
        return """
                Para registrar una habitacion en UBIK:
                
                1. Inicia sesion con tu cuenta (Propietario)
                2. En el dashboard operacional dar clic en agregar nueva habitacion
                3. Completa el formulario y confirma la creacion
                
                Recuerda que puedes modificar la informacion en cualquier momento.
                
                """;
    }

    private String getGeneralInfo() {
        return """
                En UBIK puedes:
                
                🔎 Buscar moteles y ver sus habitaciones
                🛏 Consultar disponibilidad
                📅 Realizar y gestionar reservas
                👤 Administrar tu perfil
                
                Tipos de usuario:
                - Usuario anónimo: solo consulta
                - Usuario registrado: puede reservar
                - Propietario: administra moteles y habitaciones
                
                También puedes preguntarme cosas como:
                - "ver moteles disponibles"
                - "mis reservas"
                - "habitaciones de un motel"
                - "ver mi perfil"
                """;
    }

}