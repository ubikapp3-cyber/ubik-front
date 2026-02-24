package com.ubik.usermanagement.infrastructure.adapter.out.repository;

import com.ubik.usermanagement.application.port.out.UserRepositoryPort;
import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.infrastructure.adapter.out.repository.entity.UserEntity;
import com.ubik.usermanagement.infrastructure.adapter.out.repository.mapper.UserMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adaptador del repositorio de usuarios
 * 
 * Principio SOLID: Dependency Inversion - Implementa puerto definido en dominio
 */
@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    public UserRepositoryAdapter(UserRepository userRepository, UserMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<User> findById(Long id) {
        return userRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken).map(mapper::toDomain);
    }

    @Override
    public Mono<User> save(User user) {
        return userRepository.save(mapper.toEntity(user)).map(mapper::toDomain);
    }

    @Override
    public Mono<User> update(User user) {
        return userRepository.findByUsername(user.username())
                .flatMap(existing -> {
                    UserEntity updatedEntity = new UserEntity(
                            existing.id(),
                            user.username(),
                            user.password(),
                            user.email(),
                            user.phoneNumber(),
                            user.createdAt(),
                            user.anonymous(),
                            user.roleId(),
                            user.resetToken(),
                            user.resetTokenExpiry(),
                            user.longitude(),
                            user.latitude(),
                            user.birthDate()
                    );
                    return userRepository.save(updatedEntity);
                })
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return userRepository.deleteById(id);
    }
}