package com.ubik.streakservice.infrastructure.adapter.in.web;

import com.ubik.streakservice.domain.port.in.StreakAdminUseCasePort;
import com.ubik.streakservice.domain.port.in.dto.AdminStreakStatsResponse;
import com.ubik.streakservice.domain.port.in.dto.AdminUserStreakResponse;
import com.ubik.streakservice.domain.port.in.dto.OverrideStreakRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/streaks/admin")
public class StreakAdminController {

    private final StreakAdminUseCasePort adminUseCase;
    private static final String ROLE_ADMIN = 
            System.getenv().getOrDefault("ROLE_ID_ADMIN", "7392841056473829");

    public StreakAdminController(StreakAdminUseCasePort adminUseCase) {
        this.adminUseCase = adminUseCase;
    }

    @GetMapping("/all")
    public Flux<AdminUserStreakResponse> getAllStreaks(@RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        validateAdminRole(role);
        return adminUseCase.getAllStreaks(null);
    }

    @GetMapping("/users")
    public Flux<AdminUserStreakResponse> getUsersByLevel(
            @RequestParam("level") String level,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        validateAdminRole(role);
        return adminUseCase.getAllStreaks(level);
    }

    @GetMapping("/stats")
    public Mono<AdminStreakStatsResponse> getStats(@RequestHeader(value = "X-User-Role", defaultValue = "") String role) {
        validateAdminRole(role);
        return adminUseCase.getStreakStats();
    }

    @PatchMapping("/{userId}/override")
    public Mono<Void> overrideStreak(
            @PathVariable Long userId,
            @RequestBody OverrideStreakRequest request,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String role,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") Long adminId) {
        validateAdminRole(role);
        return adminUseCase.overrideUserStreak(userId, request, adminId);
    }

    private void validateAdminRole(String role) {
        if (!ROLE_ADMIN.equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can access this resource");
        }
    }
}
