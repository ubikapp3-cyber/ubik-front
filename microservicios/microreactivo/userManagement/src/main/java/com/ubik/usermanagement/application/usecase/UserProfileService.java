package com.ubik.usermanagement.application.usecase;

import com.ubik.usermanagement.application.port.in.UserProfileUseCase;
import com.ubik.usermanagement.application.port.out.UserRepositoryPort;
import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UserProfileResponse;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio de perfil de usuario
 * 
 * Principio SOLID: Single Responsibility - Solo maneja lógica de perfil
 */
@Service
public class UserProfileService implements UserProfileUseCase {

    private final UserRepositoryPort userRepository;

    public UserProfileService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserProfileResponse> getUserProfile(String username) {
        return userRepository.findByUsername(username)
                .map(this::toResponse);
    }

    @Override
    public Mono<UserProfileResponse> updateUserProfile(String username, UpdateUserRequest request) {
        return userRepository.findByUsername(username)
                .flatMap(existingUser -> {
                    User updatedUser = new User(
                            existingUser.id(),
                            existingUser.username(),
                            existingUser.password(),
                            request.email() != null ? request.email() : existingUser.email(),
                            request.phoneNumber() != null ? request.phoneNumber() : existingUser.phoneNumber(),
                            existingUser.createdAt() != null ? existingUser.createdAt() : LocalDateTime.now(),
                            request.anonymous() != null ? request.anonymous() : existingUser.anonymous(),
                            existingUser.roleId(),
                            existingUser.resetToken(),
                            existingUser.resetTokenExpiry(),
                            // Nuevos campos con lógica de actualización
                            request.longitude() != null ? request.longitude() : existingUser.longitude(),
                            request.latitude() != null ? request.latitude() : existingUser.latitude(),
                            request.birthDate() != null ? request.birthDate() : existingUser.birthDate()
                    );

                    return userRepository.save(updatedUser)
                            .map(this::toResponse);
                });
    }

    @Override
    public Mono<UserProfileResponse> getUserProfileById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse);
    }

    @Override
    public Mono<Boolean> deleteUserProfile(String username) {

        return userRepository.findByUsername(username)
                .flatMap(user ->
                        userRepository.deleteById(user.id())
                                .thenReturn(true)
                )
                .defaultIfEmpty(false);
    }

    /**
     * Convierte User a UserProfileResponse
     * Incluye cálculo de edad
     */
    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.id(),
                user.username(),
                user.email(),
                user.phoneNumber(),
                user.createdAt(),
                user.anonymous(),
                user.roleId(),
                user.longitude(),
                user.latitude(),
                user.birthDate(),
                user.calculateAge()  
        );
    }
}