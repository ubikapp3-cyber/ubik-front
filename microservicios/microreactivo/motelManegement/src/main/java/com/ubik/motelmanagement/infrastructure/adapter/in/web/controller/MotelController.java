package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.UserR2dbcRepository;
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
    private final UserR2dbcRepository userRepository;

    public MotelController(
            MotelUseCasePort motelUseCasePort,
            MotelDtoMapper motelDtoMapper,
            UserR2dbcRepository userRepository,
            @Value("${app.roles.admin:#{environment['ROLE_ID_ADMIN']}}") String roleIdAdmin,
            @Value("${app.roles.property-owner:#{environment['ROLE_ID_PROPERTY_OWNER']}}") String roleIdPropertyOwner) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
        this.userRepository = userRepository;
        this.roleIdAdmin = roleIdAdmin;
        this.roleIdPropertyOwner = roleIdPropertyOwner;
    }

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MotelResponse> createMotel(
            @Valid @RequestBody CreateMotelRequest request,
            ServerWebExchange exchange) {

        log.info("Recibida solicitud para crear motel: {}", request.name());
        log.debug("Payload recibido: {}", request);

        String username    = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role        = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        }

        if (!isAdmin(role) && !isPropertyOwner(role)) {
            log.warn("Usuario {} con rol {} intentó crear un motel sin permisos", username, role);
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso"));
        }

        return resolveUserId(userIdHeader, username)
                .flatMap(userId -> {
                    log.info("Creando motel para userId: {} (username: '{}')", userId, username);
                    Motel motel = motelDtoMapper.toDomain(request);
                    Motel motelWithOwner = new Motel(
                            null, motel.name(), motel.address(), motel.phoneNumber(),
                            motel.description(), motel.city(), userId,
                            motel.dateCreated(), motel.imageUrls(),
                            motel.latitude(), motel.longitude(),
                            motel.approvalStatus(), motel.approvalDate(),
                            motel.approvedByUserId(), motel.rejectionReason(),
                            motel.rues(), motel.rnt(), motel.ownerDocumentType(),
                            motel.ownerDocumentNumber(), motel.ownerFullName(),
                            motel.legalRepresentativeName(), motel.legalDocumentUrl()
                    );
                    return motelUseCasePort.createMotel(motelWithOwner);
                })
                .map(motelDtoMapper::toResponse);
    }

    private Mono<Long> resolveUserId(String userIdHeader, String username) {
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                return Mono.just(Long.parseLong(userIdHeader));
            } catch (NumberFormatException e) {
                log.warn("Header X-User-Id no es numérico: {}. Cayendo a búsqueda en BD.", userIdHeader);
            }
        }
        return userRepository.findIdByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado: " + username)));
    }

    @PutMapping("/{id}")
    public Mono<MotelResponse> updateMotel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMotelRequest request,
            ServerWebExchange exchange) {

        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role     = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        }

        if (isAdmin(role)) {
            return Mono.just(request)
                    .map(motelDtoMapper::toDomain)
                    .flatMap(motel -> motelUseCasePort.updateMotel(id, motel))
                    .map(motelDtoMapper::toResponse);
        }

        if (!isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso"));
        }

        return userRepository.findIdByUsername(username)
                .flatMap(userId -> motelUseCasePort.getMotelById(id)
                        .flatMap(existingMotel -> {
                            if (!userId.equals(existingMotel.propertyId())) {
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
                            }
                            return Mono.just(request)
                                    .map(motelDtoMapper::toDomain)
                                    .flatMap(motel -> motelUseCasePort.updateMotel(id, motel));
                        })
                )
                .map(motelDtoMapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMotel(@PathVariable Long id, ServerWebExchange exchange) {
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role     = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        }

        if (isAdmin(role)) {
            return motelUseCasePort.deleteMotel(id);
        }

        if (!isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
        }

        return userRepository.findIdByUsername(username)
                .flatMap(userId -> motelUseCasePort.getMotelById(id)
                        .flatMap(existingMotel -> {
                            if (!userId.equals(existingMotel.propertyId())) {
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
                            }
                            return motelUseCasePort.deleteMotel(id);
                        })
                );
    }

    private boolean isAdmin(String role) {
        return roleIdAdmin != null && roleIdAdmin.equals(role);
    }

    private boolean isPropertyOwner(String role) {
        return roleIdPropertyOwner != null && roleIdPropertyOwner.equals(role);
    }
}