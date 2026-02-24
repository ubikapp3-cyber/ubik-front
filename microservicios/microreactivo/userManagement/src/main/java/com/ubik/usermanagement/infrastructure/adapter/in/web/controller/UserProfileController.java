package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.application.port.in.UserProfileUseCase;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    private final UserProfileUseCase userProfileUseCase;

    public UserProfileController(UserProfileUseCase userProfileUseCase) {
        this.userProfileUseCase = userProfileUseCase;
    }

    @GetMapping
    public Mono<ResponseEntity<UserProfileResponse>> getProfile(ServerWebExchange exchange) {
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");

        return userProfileUseCase.getUserProfile(username)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserProfileResponse>> getById(@PathVariable Long id) {
        return userProfileUseCase.getUserProfileById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping
    public Mono<ResponseEntity<UserProfileResponse>> updateProfile(
            @RequestBody UpdateUserRequest request,
            ServerWebExchange exchange) {

        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");

        return userProfileUseCase.updateUserProfile(username, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping
    public Mono<ResponseEntity<Void>> deleteProfile(ServerWebExchange exchange) {

        String username = exchange.getRequest()
                .getHeaders()
                .getFirst("X-User-Username");

        return userProfileUseCase.deleteUserProfile(username)
                .map(deleted -> deleted
                        ? ResponseEntity.noContent().build()
                        : ResponseEntity.notFound().build()
                );
    }
}
