package com.ubik.usermanagement.application.usecase;

import com.ubik.usermanagement.application.port.in.UserUseCase;
import com.ubik.usermanagement.application.port.out.NotificationPort;
import com.ubik.usermanagement.application.port.out.UserRepositoryPort;
import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ResetPasswordRequest;
import com.ubik.usermanagement.infrastructure.adapter.out.jwt.JwtAdapter;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.ubik.usermanagement.domain.exception.AccountDeletedException;
import com.ubik.usermanagement.domain.model.RoleConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de gestión de usuarios
 * ✅ CORREGIDO: Usa phoneNumber del RegisterRequest
 */
@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAdapter jwtAdapter;
    private final NotificationPort notificationPort;

    @Value("${google.client-id}")
    private String googleClientId;

    public UserService(
            UserRepositoryPort userRepository,
            PasswordEncoder passwordEncoder,
            JwtAdapter jwtAdapter,
            NotificationPort notificationPort) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAdapter = jwtAdapter;
        this.notificationPort = notificationPort;
    }

    @Override
    public Mono<String> register(RegisterRequest request) {
        return userRepository.findByUsername(request.username())
                .flatMap(existing -> Mono.<String>error(
                    new RuntimeException("Username already exists")))
                .switchIfEmpty(
                        userRepository.findByEmail(request.email())
                                .flatMap(existing -> Mono.<String>error(
                                    new RuntimeException("Email already exists")))
                                .switchIfEmpty(Mono.defer(() -> {

                                    User user = new User(
                                            null,
                                            request.username(),
                                            passwordEncoder.encode(request.password()),
                                            request.email(),
                                            request.phoneNumber(),  // ✅ CORREGIDO: Ahora usa el phoneNumber del request
                                            null,                    // createdAt (se genera en BD)
                                            request.anonymous(),
                                            request.roleId(),
                                            null,                    // resetToken
                                            null,                    // resetTokenExpiry
                                            request.longitude(),
                                            request.latitude(),
                                            request.birthDate(),
                                            null                     // deletedAt
                                    );

                                    return userRepository.save(user)
                                            //.map(saved -> jwtAdapter.generateToken(
                                              //      saved.username(),
                                                //    saved.roleId(),
                                                //    saved.id()
                                            //)
                                            .flatMap(saved ->
                                                    notificationPort
                                                            .sendRegisterEmail(saved.email(), saved.username())
                                                            .thenReturn(
                                                                    jwtAdapter.generateToken(
                                                                            saved.username(),
                                                                            saved.roleId(),
                                                                            saved.id()
                                                                    )
                                                            )
                                            );
                                }))
                );
    }

    @Override
    public Mono<String> login(LoginRequest request) {
        return userRepository.findByUsername(request.username())
                .filter(user -> passwordEncoder.matches(request.password(), user.password()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
                .filter(User::isActive)
                .switchIfEmpty(Mono.error(new AccountDeletedException("Account is deactivated")))
                .map(user -> jwtAdapter.generateToken(
                        user.username(),
                        user.roleId(),
                        user.id()
                ));
    }

    @Override
    public Mono<String> requestPasswordReset(String email) {
        String resetToken = UUID.randomUUID().toString();

        return userRepository.findByEmailIncludingDeleted(email)
                .switchIfEmpty(Mono.error(new RuntimeException("Email not found")))
                .flatMap(user -> userRepository.save(new User(
                        user.id(),
                        user.username(),
                        user.password(),
                        user.email(),
                        user.phoneNumber(),
                        user.createdAt(),
                        user.anonymous(),
                        user.roleId(),
                        resetToken,
                        LocalDateTime.now().plusHours(1),
                        user.longitude(),
                        user.latitude(),
                        user.birthDate(),
                        user.deletedAt()
                )))
                .flatMap(user ->
                        notificationPort
                                .sendPasswordRecoveryEmail(user.email(), user.username(), resetToken)
                                .thenReturn(resetToken)
                );
    }

    @Override
    public Mono<String> resetPassword(ResetPasswordRequest request) {
        return userRepository.findByResetToken(request.token())
                .filter(user -> user.resetTokenExpiry() != null 
                    && user.resetTokenExpiry().isAfter(LocalDateTime.now()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid or expired token")))
                .flatMap(user -> userRepository.save(new User(
                        user.id(),
                        user.username(),
                        passwordEncoder.encode(request.newPassword()),
                        user.email(),
                        user.phoneNumber(),
                        user.createdAt(),
                        user.anonymous(),
                        user.roleId(),
                        null,
                        null,
                        user.longitude(),
                        user.latitude(),
                        user.birthDate(),
                        null
                )))
                .map(user -> "Password reset successfully");
    }

    @Override
    public Mono<String> loginWithGoogle(String idToken) {
        return Mono.fromCallable(() -> verifyGoogleToken(idToken))    // blocking I/O → fromCallable
                .subscribeOn(Schedulers.boundedElastic())             // no bloquear el event loop
                .flatMap(payload -> {
                    String email = payload.getEmail();
                    String name  = payload.get("name") != null
                            ? payload.get("name").toString()
                            : email.split("@")[0];

                    return userRepository.findByEmail(email)
                            .flatMap(existing -> {
                                if (!existing.isActive()) {
                                    return Mono.error(new AccountDeletedException("Account is deactivated"));
                                }
                                return Mono.just(jwtAdapter.generateToken(
                                        existing.username(),
                                        existing.roleId(),
                                        existing.id()
                                ));
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                // Usuario nuevo → crear con roleId USER por defecto
                                User newUser = new User(
                                        null,
                                        name,
                                        passwordEncoder.encode(UUID.randomUUID().toString()), // pass inutil
                                        email,
                                        null,
                                        null,
                                        false,
                                        RoleConstants.USER,
                                        null, null, null, null, null,
                                        null // deletedAt
                                );
                                return userRepository.save(newUser)
                                        .flatMap(saved ->
                                                notificationPort
                                                        .sendRegisterEmail(saved.email(), saved.username())
                                                        .thenReturn(jwtAdapter.generateToken(
                                                                saved.username(),
                                                                saved.roleId(),
                                                                saved.id()
                                                        ))
                                        );
                            }));
                });
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                    .Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId)) // @Value
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new IllegalArgumentException("Invalid Google token");
            }
            return token.getPayload();
        } catch (Exception e) {
            throw new IllegalArgumentException("Google token verification failed", e);
        }
    }
}