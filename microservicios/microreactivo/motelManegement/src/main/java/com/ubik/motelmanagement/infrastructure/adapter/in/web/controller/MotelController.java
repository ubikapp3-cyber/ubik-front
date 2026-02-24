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

    // Leídos desde variables de entorno, igual que en SecurityConfig del gateway
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

    // =========================================================================
    // ENDPOINTS PÚBLICOS
    // =========================================================================

    /**
     * PÚBLICO - Obtiene todos los moteles
     * GET /api/motels
     */
    @GetMapping
    public Flux<MotelResponse> getAllMotels() {
        return motelUseCasePort.getAllMotels()
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene un motel por ID
     * GET /api/motels/{id}
     */
    @GetMapping("/{id}")
    public Mono<MotelResponse> getMotelById(@PathVariable Long id) {
        return motelUseCasePort.getMotelById(id)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene moteles por ciudad
     * GET /api/motels/city/{city}
     */
    @GetMapping("/city/{city}")
    public Flux<MotelResponse> getMotelsByCity(@PathVariable String city) {
        return motelUseCasePort.getMotelsByCity(city)
                .map(motelDtoMapper::toResponse);
    }

    // =========================================================================
    // ENDPOINTS PROTEGIDOS
    // =========================================================================

    /**
     * PROTEGIDO - Crea un nuevo motel.
     * El propertyId se asigna desde el usuario autenticado (lookup por username).
     * Cualquier propertyId en el body es ignorado.
     * POST /api/motels
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MotelResponse> createMotel(
            @Valid @RequestBody CreateMotelRequest request,
            ServerWebExchange exchange) {

        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role     = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        }

        // Solo admin y property owner pueden crear moteles
        if (!isAdmin(role) && !isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para crear moteles"));
        }

        log.info("Creando motel para username: '{}' con role: '{}'", username, role);

        return userRepository.findIdByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado: " + username)))
                .flatMap(userId -> {
                    log.info("UserId resuelto: {} para username: '{}'", userId, username);

                    Motel motel = motelDtoMapper.toDomain(request);

                    // Forzar propertyId desde el usuario autenticado,
                    // ignorando lo que venga en el request body
                    Motel motelWithOwner = new Motel(
                            motel.id(),
                            motel.name(),
                            motel.address(),
                            motel.phoneNumber(),
                            motel.description(),
                            motel.city(),
                            userId,                 // propertyId = userId real de la BD
                            motel.dateCreated(),
                            motel.imageUrls(),
                            motel.latitude(),
                            motel.longitude(),
                            motel.approvalStatus(),
                            motel.approvalDate(),
                            motel.approvedByUserId(),
                            motel.rejectionReason(),
                            motel.rues(),
                            motel.rnt(),
                            motel.ownerDocumentType(),
                            motel.ownerDocumentNumber(),
                            motel.ownerFullName(),
                            motel.legalRepresentativeName(),
                            motel.legalDocumentUrl()
                    );

                    return motelUseCasePort.createMotel(motelWithOwner);
                })
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Actualiza un motel existente.
     * Admin puede actualizar cualquier motel.
     * Property owner solo puede actualizar sus propios moteles.
     * PUT /api/motels/{id}
     */
    @PutMapping("/{id}")
    public Mono<MotelResponse> updateMotel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMotelRequest request,
            ServerWebExchange exchange) {

        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role     = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        }

        log.info("Actualizando motel {} por username: '{}' con role: '{}'", id, username, role);

        // Admin puede actualizar cualquier motel sin verificar propiedad
        if (isAdmin(role)) {
            return Mono.just(request)
                    .map(motelDtoMapper::toDomain)
                    .flatMap(motel -> motelUseCasePort.updateMotel(id, motel))
                    .map(motelDtoMapper::toResponse);
        }

        // Property owner: verificar que el motel le pertenece
        if (!isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para modificar moteles"));
        }

        return userRepository.findIdByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado: " + username)))
                .flatMap(userId ->
                        motelUseCasePort.getMotelById(id)
                                .flatMap(existingMotel -> {
                                    if (!userId.equals(existingMotel.propertyId())) {
                                        log.warn("Acceso denegado: usuario {} intentó editar motel {} (propietario: {})",
                                                userId, id, existingMotel.propertyId());
                                        return Mono.error(new ResponseStatusException(
                                                HttpStatus.FORBIDDEN,
                                                "No tienes permiso para modificar este motel"));
                                    }
                                    return Mono.just(request)
                                            .map(motelDtoMapper::toDomain)
                                            .flatMap(motel -> motelUseCasePort.updateMotel(id, motel));
                                })
                )
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Elimina un motel.
     * Admin puede eliminar cualquier motel.
     * Property owner solo puede eliminar sus propios moteles.
     * DELETE /api/motels/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMotel(
            @PathVariable Long id,
            ServerWebExchange exchange) {

        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role     = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Usuario no autenticado"));
        }

        log.info("Eliminando motel {} por username: '{}' con role: '{}'", id, username, role);

        // Admin puede eliminar cualquier motel
        if (isAdmin(role)) {
            return motelUseCasePort.deleteMotel(id);
        }

        // Property owner: verificar propiedad antes de eliminar
        if (!isPropertyOwner(role)) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para eliminar moteles"));
        }

        return userRepository.findIdByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado: " + username)))
                .flatMap(userId ->
                        motelUseCasePort.getMotelById(id)
                                .flatMap(existingMotel -> {
                                    if (!userId.equals(existingMotel.propertyId())) {
                                        log.warn("Acceso denegado: usuario {} intentó eliminar motel {} (propietario: {})",
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