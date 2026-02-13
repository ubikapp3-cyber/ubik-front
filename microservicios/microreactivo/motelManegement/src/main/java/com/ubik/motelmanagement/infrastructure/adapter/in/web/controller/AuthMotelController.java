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

/**
 * Controlador REST para operaciones autenticadas de Motel
 *
 * MODIFICADO: Ahora busca por USERNAME en lugar de userId
 * ya que el JWT solo contiene el username, no el userId numérico.
 *
 * FLUJO:
 * 1. JWT contiene: { "sub": "admin", "role": "1" }
 * 2. Gateway propaga: X-User-Username: admin
 * 3. Controlador busca: SELECT id FROM users WHERE username = 'admin'
 * 4. Con el userId obtenido, busca: SELECT * FROM motel WHERE property_id = userId
 */
@RestController
@RequestMapping("/api/auth/motels")
public class AuthMotelController {

    private static final Logger log = LoggerFactory.getLogger(AuthMotelController.class);

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;
    private final UserR2dbcRepository userRepository;  // ✅ NUEVO

    public AuthMotelController(
            MotelUseCasePort motelUseCasePort,
            MotelDtoMapper motelDtoMapper,
            UserR2dbcRepository userRepository) {  // ✅ NUEVO
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
        this.userRepository = userRepository;
    }

    /**
     * PROTEGIDO - Obtiene los moteles del usuario autenticado (por USERNAME)
     * GET /api/auth/motels/my-motels
     *
     * Este endpoint:
     * 1. Lee el username del header X-User-Username (viene del JWT)
     * 2. Busca el userId en la tabla users por username
     * 3. Busca los moteles donde property_id = userId
     * 4. Devuelve los moteles del usuario
     *
     * @param exchange ServerWebExchange para obtener headers de autenticación
     * @return Flux con los moteles del propietario autenticado
     */
    @GetMapping("/my-motels")
    public Flux<MotelResponse> getMyMotels(ServerWebExchange exchange) {

        log.info("╔═══════════════════════════════════════════════════════════════╗");
        log.info("║   GET /api/auth/motels/my-motels - Buscar por USERNAME      ║");
        log.info("╚═══════════════════════════════════════════════════════════════╝");

        // Obtener username del header (viene del JWT vía Gateway)
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        log.info("📋 Headers recibidos:");
        log.info("   ├─ X-User-Username: {}", username);
        log.info("   └─ X-User-Role: {}", role);

        // Validar que el header X-User-Username existe
        if (username == null || username.isBlank()) {
            log.error("❌ Usuario no autenticado - Header X-User-Username ausente");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado. Se requiere el header X-User-Username");
        }

        log.info("🔍 Paso 1: Buscando userId para username: '{}'", username);

        // ═══════════════════════════════════════════════════════════════
        // 🔧 SOLUCIÓN: Buscar userId por username
        // ═══════════════════════════════════════════════════════════════
        return userRepository.findIdByUsername(username)
                .doOnNext(userId -> log.info("   ✅ UserId encontrado: {}", userId))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("   ❌ No se encontró usuario con username: '{}'", username);
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Usuario no encontrado: " + username));
                }))
                .flatMapMany(userId -> {
                    log.info("🔍 Paso 2: Buscando moteles donde property_id = {}", userId);
                    log.info("   Query: SELECT * FROM motel WHERE property_id = {}", userId);

                    return motelUseCasePort.getMotelsByPropertyId(userId)
                            .doOnSubscribe(subscription ->
                                    log.debug("   ⏳ Ejecutando query en repositorio..."))
                            .doOnNext(motel ->
                                    log.info("   ✓ Motel encontrado: id={}, name='{}', propertyId={}",
                                            motel.id(), motel.name(), motel.propertyId()))
                            .doOnComplete(() -> {
                                log.info("✅ Búsqueda completada para username: '{}'", username);
                                log.info("═══════════════════════════════════════════════════════════════");
                            })
                            .doOnError(error -> {
                                log.error("❌ Error buscando moteles para username '{}': {}",
                                        username, error.getMessage(), error);
                                log.error("═══════════════════════════════════════════════════════════════");
                            })
                            .switchIfEmpty(Flux.defer(() -> {
                                log.warn("⚠️  No se encontraron moteles para username: '{}'", username);
                                log.info("💡 Posibles causas:");
                                log.info("   1. El usuario no ha creado ningún motel");
                                log.info("   2. Los moteles tienen property_id diferente al userId");
                                log.info("   3. Los moteles están en estado PENDING/REJECTED");
                                return Flux.empty();
                            }));
                })
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Obtiene moteles de un usuario específico por userId
     * GET /api/auth/motels/{userId}
     *
     * DEPRECADO: Este endpoint se mantiene para retrocompatibilidad,
     * pero se recomienda usar /my-motels en su lugar.
     *
     * @param userId ID del propietario
     * @param exchange ServerWebExchange para obtener headers de autenticación
     * @return Flux con los moteles del propietario
     */
    @GetMapping("/{userId}")
    public Flux<MotelResponse> getMotelsByAuthenticatedUser(
            @PathVariable Long userId,
            ServerWebExchange exchange) {

        log.info("=== GET /api/auth/motels/{} ===", userId);
        log.warn("⚠️  Usando endpoint deprecado. Recomendación: usar /api/auth/motels/my-motels");

        String authenticatedUserIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");

        log.debug("Headers recibidos:");
        log.debug("  X-User-Id: {}", authenticatedUserIdHeader);
        log.debug("  X-User-Username: {}", username);
        log.debug("  X-User-Role: {}", role);

        // Si no hay X-User-Id pero hay username, redirigir a búsqueda por username
        if (authenticatedUserIdHeader == null && username != null) {
            log.info("⚠️  No hay X-User-Id en header, redirigiendo a búsqueda por username");
            return getMyMotels(exchange);
        }

        // Validación original del userId en header
        if (authenticatedUserIdHeader == null || authenticatedUserIdHeader.isBlank()) {
            log.error("Usuario no autenticado - Header X-User-Id ausente");
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado. Se requiere el header X-User-Id");
        }

        Long authenticatedUserId;
        try {
            authenticatedUserId = Long.parseLong(authenticatedUserIdHeader);
            log.info("Usuario autenticado ID parseado: {}", authenticatedUserId);
        } catch (NumberFormatException e) {
            log.error("Error parseando X-User-Id: {}", authenticatedUserIdHeader, e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El header X-User-Id debe ser un número válido");
        }

        // Validar que el userId en la URL coincide con el usuario autenticado
        if (!authenticatedUserId.equals(userId)) {
            log.warn("Acceso denegado: Usuario {} intentó acceder a moteles de usuario {}",
                    authenticatedUserId, userId);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para acceder a los moteles de otro usuario");
        }

        // Buscar moteles del propietario
        log.info("Buscando moteles donde motel.property_id = {}", userId);

        return motelUseCasePort.getMotelsByPropertyId(userId)
                .doOnNext(motel -> log.debug("Motel encontrado: id={}, name={}, propertyId={}",
                        motel.id(), motel.name(), motel.propertyId()))
                .doOnComplete(() -> log.info("Búsqueda completada para userId: {}", userId))
                .doOnError(error -> log.error("Error buscando moteles para userId {}: {}",
                        userId, error.getMessage(), error))
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn("No se encontraron moteles para userId: {}", userId);
                    return Flux.empty();
                }))
                .map(motelDtoMapper::toResponse);
    }
}