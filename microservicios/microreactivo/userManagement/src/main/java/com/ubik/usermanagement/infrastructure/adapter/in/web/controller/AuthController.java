package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.application.port.in.UserUseCase;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ResetPasswordRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.GoogleAuthRequest;
import com.ubik.usermanagement.domain.exception.AccountDeletedException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserUseCase userUseCase;

    public AuthController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> register(@Valid @RequestBody RegisterRequest request) {
        return userUseCase.register(request);
    }

    @PostMapping("/login")
    public Mono<String> login(@Valid @RequestBody LoginRequest request) {
        return userUseCase.login(request);
    }

    @PostMapping("/google")
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        return userUseCase.loginWithGoogle(request.idToken());
    }

    @PostMapping("/reset-password-request")
    public Mono<String> requestPasswordReset(@RequestParam String email) {
        return userUseCase.requestPasswordReset(email);
    }

    @PostMapping("/reset-password")
    public Mono<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return userUseCase.resetPassword(request);
    }

    @GetMapping("/admin/test")
    public Mono<String> adminTest() {
        return Mono.just("Admin access granted");
    }

    @GetMapping("/user/test")
    public Mono<String> userTest() {
        return Mono.just("User or Client access granted");
    }

    @ExceptionHandler(AccountDeletedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<String> handleAccountDeleted(AccountDeletedException ex) {
        return Mono.just(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleInvalidCredentials(RuntimeException ex) {
        return Mono.just("Error"+ex.getMessage());
    }
}
