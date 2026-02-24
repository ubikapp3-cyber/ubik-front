package com.ubik.motelmanagement.infrastructure.adapter.in.web.security;

import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.UserR2dbcRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Componente utilitario para resolver el contexto de autenticación
 * desde los headers propagados por el Gateway.
 *
 * FUENTE DE VERDAD: siempre resuelve userId por username contra la BD local,
 * nunca confía en X-User-Id directamente (el claim JWT puede ser 0 o inconsistente).
 */
@Component
public class AuthContextResolver {

    private final UserR2dbcRepository userRepository;

    public AuthContextResolver(UserR2dbcRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Extrae el username del header X-User-Username y busca el userId en la BD.
     * Lanza 401 si el header está ausente, 404 si el usuario no existe.
     *
     * @param exchange ServerWebExchange con los headers de la request
     * @return Mono<Long> con el userId real de la base de datos
     */
    public Mono<Long> resolveUserId(ServerWebExchange exchange) {
        String username = extractUsername(exchange);

        if (username == null || username.isBlank()) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Header X-User-Username ausente. El usuario no está autenticado."));
        }

        return userRepository.findIdByUsername(username)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado: " + username)));
    }

    /**
     * Extrae el username del header X-User-Username.
     * Devuelve null si el header no está presente.
     */
    public String extractUsername(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-User-Username");
    }

    /**
     * Extrae el role del header X-User-Role.
     * Devuelve null si el header no está presente.
     */
    public String extractRole(ServerWebExchange exchange) {
        return exchange.getRequest().getHeaders().getFirst("X-User-Role");
    }

    /**
     * Valida que el usuario esté autenticado (username presente).
     * Lanza 401 si no lo está.
     */
    public void requireAuthenticated(ServerWebExchange exchange) {
        String username = extractUsername(exchange);
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado. Se requiere el header X-User-Username.");
        }
    }
}