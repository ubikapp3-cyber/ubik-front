package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.UserR2dbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth/motels")
public class AuthMotelController {

    private static final Logger log = LoggerFactory.getLogger(AuthMotelController.class);

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;
    private final UserR2dbcRepository userRepository;

    public AuthMotelController(
            MotelUseCasePort motelUseCasePort,
            MotelDtoMapper motelDtoMapper,
            UserR2dbcRepository userRepository) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-motels")
    public Flux<MotelResponse> getMyMotels(ServerWebExchange exchange) {
        log.info("GET /api/auth/motels/my-motels");

        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null || username.isBlank()) {
            return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        }

        return userRepository.findIdByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + username)))
                .flatMapMany(userId -> motelUseCasePort.getMotelsByPropertyId(userId))
                .map(motelDtoMapper::toResponse);
    }

    @GetMapping("/{userId}")
    public Flux<MotelResponse> getMotelsByAuthenticatedUser(
            @PathVariable Long userId,
            ServerWebExchange exchange) {

        log.info("GET /api/auth/motels/{}", userId);

        String authIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if (authIdHeader == null || authIdHeader.isBlank()) {
            return Flux.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        }

        try {
            Long authenticatedUserId = Long.parseLong(authIdHeader);
            if (!authenticatedUserId.equals(userId)) {
                return Flux.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado"));
            }
        } catch (NumberFormatException e) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID invalido"));
        }

        return motelUseCasePort.getMotelsByPropertyId(userId)
                .map(motelDtoMapper::toResponse);
    }
}
