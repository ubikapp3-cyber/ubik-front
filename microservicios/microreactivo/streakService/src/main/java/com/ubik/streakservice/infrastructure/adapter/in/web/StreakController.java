package com.ubik.streakservice.infrastructure.adapter.in.web;

import com.ubik.streakservice.domain.port.in.StreakUseCasePort;
import com.ubik.streakservice.infrastructure.adapter.in.web.dto.StreakResponse;
import com.ubik.streakservice.infrastructure.adapter.in.web.mapper.StreakDtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/streaks")
public class StreakController {

    private final StreakUseCasePort streakUseCase;
    private final StreakDtoMapper mapper;

    public StreakController(StreakUseCasePort streakUseCase, StreakDtoMapper mapper) {
        this.streakUseCase = streakUseCase;
        this.mapper = mapper;
    }

    /**
     * GET /api/streaks/{userId}
     * Requiere autenticación. Solo el propio usuario o ADMIN pueden consultar.
     */
    @GetMapping("/{userId}")
    public Mono<StreakResponse> getStreak(
            @PathVariable Long userId,
            ServerWebExchange exchange) {

        validateAccess(userId, exchange);
        return streakUseCase.getStreak(userId).map(mapper::toResponse);
    }

    /**
     * GET /api/streaks/me
     * Shortcut: el usuario autenticado consulta su propia racha.
     */
    @GetMapping("/me")
    public Mono<StreakResponse> getMyStreak(ServerWebExchange exchange) {
        Long userId = extractUserId(exchange);
        return streakUseCase.getStreak(userId).map(mapper::toResponse);
    }

    /**
     * POST /api/streaks/{userId}/recalculate
     * Endpoint interno. Lo llama motel-management tras confirmar una reserva.
     */
    @PostMapping("/{userId}/recalculate")
    @ResponseStatus(HttpStatus.OK)
    public Mono<StreakResponse> recalculate(
            @PathVariable Long userId,
            @RequestHeader(value = "X-Internal-Request", required = false) String internal) {

        if (!"true".equals(internal)) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
        }
        return streakUseCase.recalculate(userId).map(mapper::toResponse);
    }

    /**
     * GET /api/streaks/{userId}/discount?price=150000
     * Calcula el precio con el descuento de racha aplicado.
     */
    @GetMapping("/{userId}/discount")
    public Mono<DiscountResponse> calculateDiscount(
            @PathVariable Long userId,
            @RequestParam double price,
            ServerWebExchange exchange) {

        validateAccess(userId, exchange);
        return streakUseCase.applyDiscount(userId, price)
                .map(finalPrice -> new DiscountResponse(price, finalPrice, price - finalPrice));
    }

    // ─── helpers ───────────────────────────────────────────────────────────

    private void validateAccess(Long userId, ServerWebExchange exchange) {
        String authId  = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String role    = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        String adminId = System.getenv().getOrDefault("ROLE_ID_ADMIN", "");

        if (authId == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        boolean isAdmin = adminId.equals(role);
        boolean isSelf  = userId.toString().equals(authId);

        if (!isAdmin && !isSelf) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private Long extractUserId(ServerWebExchange exchange) {
        String id = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        try { return Long.parseLong(id); }
        catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-User-Id inválido");
        }
    }

    public record DiscountResponse(double originalPrice, double finalPrice, double discount) {}
}
