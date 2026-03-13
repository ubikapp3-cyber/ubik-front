package com.ubik.usermanagement.application.port.in;

import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ResetPasswordRequest;
import reactor.core.publisher.Mono;

public interface UserUseCase {
    Mono<String> register(RegisterRequest request);
    Mono<String> login(LoginRequest request);
    Mono<String> requestPasswordReset(String email);
    Mono<String> resetPassword(ResetPasswordRequest request);
    Mono<String> loginWithGoogle(String idToken);
}
