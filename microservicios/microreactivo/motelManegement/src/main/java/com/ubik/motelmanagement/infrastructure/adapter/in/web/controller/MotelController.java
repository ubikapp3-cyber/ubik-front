package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.security.AuthContextResolver;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/motels")
public class MotelController {

    private static final Logger log = LoggerFactory.getLogger(MotelController.class);

    private final String roleIdAdmin;
    private final String roleIdPropertyOwner;

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;
    private final AuthContextResolver authContextResolver;

    public MotelController(
            MotelUseCasePort motelUseCasePort,
            MotelDtoMapper motelDtoMapper,
            AuthContextResolver authContextResolver,
            @Value("${app.roles.admin:#{environment['ROLE_ID_ADMIN']}}") String roleIdAdmin,
            @Value("${app.roles.property-owner:#{environment['ROLE_ID_PROPERTY_OWNER']}}") String roleIdPropertyOwner) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
        this.authContextResolver = authContextResolver;
        this.roleIdAdmin = roleIdAdmin;
        this.roleIdPropertyOwner = roleIdPropertyOwner;
    }

    // =========================================================================
    // ENDPOINTS PÚBLICOS
    // =========================================================================

    @GetMapping
    public Flux<MotelResponse> getAllMotels() {
        return motelUseCasePort.getAllMotels()
                .map(motelDtoMapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<MotelResponse> getMotelById(@PathVariable Long id) {
        return motelUseCasePort.getMotelById(id)
                .map(motelDtoMapper::toResponse);
    }

    @GetMapping("/city/{city}")
    public Flux<MotelResponse> getMotelsByCity(@PathVariable String city) {
        return motelUseCasePort.getMotelsByCity(city)
                .map(motelDtoMapper::toResponse);
    }

    // =========================================================================
    // ENDPOINTS PROTEGIDOS
    // =========================================================================

    /**
     * POST /api/motels
     * Crea un nuevo motel asignando el propertyId desde el usuario autenticado.
     * Solo ADMIN y PROPERTY_OWNER pueden crear moteles.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MotelResponse> createMotel(
            @Valid @RequestBody CreateMotelRequest request,
            ServerWebExchange exchange) {

        String role = authContextResolver.extractRole(exchange);

        if (!isAdmin(role) && !isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para crear moteles"));
        }

        log.info("Creando motel — username: '{}', role: '{}'",
                authContextResolver.extractUsername(exchange), role);

        return authContextResolver.resolveUserId(exchange)
                .flatMap(userId -> {
                    log.info("UserId resuelto: {} para motel '{}'", userId, request.name());
                    Motel motel = motelDtoMapper.toDomainWithOwner(request, userId);
                    return motelUseCasePort.createMotel(motel);
                })
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PUT /api/motels/{id}
     * Admin actualiza cualquier motel. Property owner solo los suyos.
     */
    @PutMapping("/{id}")
    public Mono<MotelResponse> updateMotel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMotelRequest request,
            ServerWebExchange exchange) {

        String role = authContextResolver.extractRole(exchange);

        log.info("Actualizando motel {} — username: '{}', role: '{}'",
                id, authContextResolver.extractUsername(exchange), role);

        if (isAdmin(role)) {
            return Mono.just(request)
                    .map(motelDtoMapper::toDomain)
                    .flatMap(motel -> motelUseCasePort.updateMotel(id, motel))
                    .map(motelDtoMapper::toResponse);
        }

        if (!isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para modificar moteles"));
        }

        return authContextResolver.resolveUserId(exchange)
                .flatMap(userId ->
                        motelUseCasePort.getMotelById(id)
                                .flatMap(existingMotel -> {
                                    if (!userId.equals(existingMotel.propertyId())) {
                                        log.warn("Acceso denegado: userId {} intentó editar motel {} (propietario: {})",
                                                userId, id, existingMotel.propertyId());
                                        return Mono.error(new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "No tienes permiso para modificar este motel"));
                                    }
                                    Motel motel = motelDtoMapper.toDomain(request);
                                    return motelUseCasePort.updateMotel(id, motel);
                                })
                )
                .map(motelDtoMapper::toResponse);
    }

    /**
     * DELETE /api/motels/{id}
     * Admin elimina cualquier motel. Property owner solo los suyos.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMotel(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        String role = authContextResolver.extractRole(exchange);

        log.info("Eliminando motel {} — username: '{}', role: '{}'",
                id, authContextResolver.extractUsername(exchange), role);

        if (isAdmin(role)) {
            return motelUseCasePort.deleteMotel(id);
        }

        if (!isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para eliminar moteles"));
        }

        return authContextResolver.resolveUserId(exchange)
                .flatMap(userId ->
                        motelUseCasePort.getMotelById(id)
                                .flatMap(existingMotel -> {
                                    if (!userId.equals(existingMotel.propertyId())) {
                                        log.warn("Acceso denegado: userId {} intentó eliminar motel {} (propietario: {})",
                                                userId, id, existingMotel.propertyId());
                                        return Mono.error(new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "No tienes permiso para eliminar este motel"));
                                    }
                                    return motelUseCasePort.deleteMotel(id);
                                })
                );
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private boolean isAdmin(String role) {
        return roleIdAdmin != null && roleIdAdmin.equals(role);
    }

    private boolean isPropertyOwner(String role) {
        return roleIdPropertyOwner != null && roleIdPropertyOwner.equals(role);
    }
}