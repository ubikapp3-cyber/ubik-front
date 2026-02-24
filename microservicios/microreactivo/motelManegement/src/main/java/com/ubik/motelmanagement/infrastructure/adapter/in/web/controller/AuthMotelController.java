package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.security.AuthContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

/**
 * Controlador REST para operaciones autenticadas de Motel.
 *
 * FLUJO:
 * 1. JWT contiene: { "sub": "username", "role": "roleId" }
 * 2. Gateway propaga: X-User-Username: username
 * 3. AuthContextResolver resuelve: SELECT id FROM users WHERE username = ?
 * 4. Con el userId: SELECT * FROM motel WHERE property_id = userId
 */
@RestController
@RequestMapping("/api/auth/motels")
public class AuthMotelController {

    private static final Logger log = LoggerFactory.getLogger(AuthMotelController.class);

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;
    private final AuthContextResolver authContextResolver;

    public AuthMotelController(
            MotelUseCasePort motelUseCasePort,
            MotelDtoMapper motelDtoMapper,
            AuthContextResolver authContextResolver) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
        this.authContextResolver = authContextResolver;
    }

    /**
     * GET /api/auth/motels/my-motels
     * Devuelve los moteles del usuario autenticado.
     */
    @GetMapping("/my-motels")
    public Flux<MotelResponse> getMyMotels(ServerWebExchange exchange) {
        String username = authContextResolver.extractUsername(exchange);
        log.info("GET /my-motels — username: '{}'", username);

        return authContextResolver.resolveUserId(exchange)
                .doOnNext(userId -> log.info("UserId resuelto: {} para username: '{}'", userId, username))
                .flatMapMany(userId -> motelUseCasePort.getMotelsByPropertyId(userId)
                        .doOnComplete(() -> log.info("Búsqueda completada para username: '{}'", username))
                )
                .map(motelDtoMapper::toResponse);
    }

    /**
     * GET /api/auth/motels/{userId}
     *
     * @deprecated Usar /my-motels. Este endpoint se mantiene por retrocompatibilidad
     *             pero delega a getMyMotels() ignorando el userId del path.
     */
    @Deprecated
    @GetMapping("/{userId}")
    public Flux<MotelResponse> getMotelsByAuthenticatedUser(
            @PathVariable Long userId,
            ServerWebExchange exchange) {

        log.warn("Endpoint deprecado GET /api/auth/motels/{} — usar /my-motels", userId);
        return getMyMotels(exchange);
    }
}