package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Gateway filter that validates user authorization based on headers and endpoint permissions.
 *
 * Expected headers:
 * - X-User-Id: User identifier
 * - X-User-Role: User role (ADMIN, PROPERTY_OWNER, USER)
 * - X-User-Email: User email (optional)
 */
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    public AuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Extract headers
            String userId = getHeader(request, "X-User-Id");
            String userRole = getHeader(request, "X-User-Role");
            String path = request.getPath().toString();
            String method = request.getMethod().toString();

            // BYPASS PARA AI SERVICE
            if (path.startsWith("/api/ai")) {
                return chain.filter(exchange);
            }

            // Validate required headers
            if (userId == null || userId.isEmpty()) {
                return unauthorized(exchange, "Missing X-User-Id header");
            }

            if (userRole == null || userRole.isEmpty()) {
                return unauthorized(exchange, "Missing X-User-Role header");
            }

            // Validate user role
            if (!isValidRole(userRole)) {
                return unauthorized(exchange, "Invalid role: " + userRole);
            }

            // Check permissions based on role and endpoint
            if (!hasPermission(userRole, method, path, userId)) {
                return forbidden(exchange, "Insufficient permissions for " + method + " " + path);
            }

            // Add user context to downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", userRole)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private String getHeader(ServerHttpRequest request, String headerName) {
        List<String> headers = request.getHeaders().get(headerName);
        return (headers != null && !headers.isEmpty()) ? headers.get(0) : null;
    }

    private boolean isValidRole(String role) {
        return role.equals("ADMIN") ||
               role.equals("PROPERTY_OWNER") ||
               role.equals("USER");
    }

    private boolean hasPermission(String role, String method, String path, String userId) {
        // ADMIN can do everything
        if (role.equals("ADMIN")) {
            return true;
        }

        // PROPERTY_OWNER permissions
        if (role.equals("PROPERTY_OWNER")) {
            return hasPropertyOwnerPermission(method, path);
        }

        // USER permissions (most restrictive)
        if (role.equals("USER")) {
            return hasUserPermission(method, path);
        }

        return false;
    }

    private boolean hasPropertyOwnerPermission(String method, String path) {
        // Property owners can manage motels, rooms, and services
        if (path.startsWith("/api/motels")) {
            return method.equals("GET") || method.equals("POST") ||
                   method.equals("PUT") || method.equals("DELETE");
        }

        if (path.startsWith("/api/rooms")) {
            return method.equals("GET") || method.equals("POST") ||
                   method.equals("PUT") || method.equals("DELETE");
        }

        if (path.startsWith("/api/services")) {
            return method.equals("GET") || method.equals("POST") ||
                   method.equals("PUT") || method.equals("DELETE");
        }

        // Property owners can view all bookings (to manage their properties)
        if (path.startsWith("/api/bookings")) {
            return method.equals("GET") || method.equals("PUT"); // Can confirm/cancel
        }

        return false;
    }

    private boolean hasUserPermission(String method, String path) {
        // Users can only read motels, rooms, and services
        if (path.startsWith("/api/motels") ||
            path.startsWith("/api/rooms") ||
            path.startsWith("/api/services")) {
            return method.equals("GET");
        }

        // Users can create and manage their own bookings
        if (path.startsWith("/api/bookings")) {
            // Allow GET for their own bookings and POST to create
            return method.equals("GET") || method.equals("POST") || method.equals("PUT");
        }

        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("X-Error-Message", message);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().add("X-Error-Message", message);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
