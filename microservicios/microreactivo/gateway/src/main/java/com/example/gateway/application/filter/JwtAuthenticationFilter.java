package com.example.gateway.application.filter;

import com.example.gateway.domain.port.out.JwtValidatorPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Filtro JWT para Spring Cloud Gateway
 * 
 * CRÍTICO: Ignora OPTIONS requests para permitir CORS preflight
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtValidatorPort jwtValidatorPort;

    public JwtAuthenticationFilter(JwtValidatorPort jwtValidatorPort) {
        this.jwtValidatorPort = jwtValidatorPort;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        // CRÍTICO: Ignorar OPTIONS (CORS preflight)
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 1. Rutas completamente públicas (no requieren JWT)
        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        // 2. Extraer JWT: 1) Authorization Header, 2) access_token query param, 3) token query param
        String token = null;
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            // Soporte para EventSource (SSE) que no permite custom headers
            token = exchange.getRequest().getQueryParams().getFirst("access_token");
            if (token == null) {
                token = exchange.getRequest().getQueryParams().getFirst("token");
            }
        }

        if (token != null) token = token.trim();

        if (token == null || token.isEmpty()) {
            System.out.println("No token found for path: " + path);
            return unauthorized(exchange);
        }

        try {
            // 3. Validar token y extraer claims
            Map<String, Object> claims = jwtValidatorPort.validateToken(token);

            String username = claims.get("sub").toString();
            Object roleObj = claims.get("role");
            Object userIdObj = claims.get("userId");
            
            String role = roleObj != null ? roleObj.toString() : "USER";
            String userId = userIdObj != null ? userIdObj.toString() : "0";

            // 4. Mutar request y agregar headers para microservicios
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Username", username)
                    .header("X-User-Role", role)
                    .header("X-User-Id", userId)
                    .header("Authorization", "Bearer " + token) // Asegurar que llegue al microservicio
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            // 5. Registrar Authentication (solo útil en el Gateway)
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));

        } catch (Exception e) {
            System.err.println("JWT Verification failed: " + e.getMessage());
            return unauthorized(exchange);
        }
    }

    /**
     * Determina si una ruta es pública (no requiere autenticación)
     */
    private boolean isPublicPath(String path, HttpMethod method) {
        // Rutas de autenticación (registro, login, etc.)
        // IMPORTANTE: /api/auth/motels/** NO es pública, requiere autenticación
        if (path.startsWith("/api/auth/") && !path.startsWith("/api/auth/motels/")) {
            return true;
        }
        
        // Actuator endpoints
        if (path.startsWith("/actuator/")) {
            return true;
        }
        
        // SOLO LECTURA (GET) es pública para estos recursos
        if (method == HttpMethod.GET) {
            return path.startsWith("/api/motels") 
                || path.startsWith("/api/rooms") 
                || path.startsWith("/api/services");
        }

        // Webhook de Stripe (Stripe no envía JWT)
        if (method == HttpMethod.POST && path.equals("/api/payments/webhook")) {
            return true;
        }
        
        // Todo lo demás requiere autenticación
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}