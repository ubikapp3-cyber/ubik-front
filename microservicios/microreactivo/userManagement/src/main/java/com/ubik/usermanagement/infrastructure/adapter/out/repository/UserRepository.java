package com.ubik.usermanagement.infrastructure.adapter.out.repository;

import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.infrastructure.adapter.out.repository.entity.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends R2dbcRepository<UserEntity, Long> {
    Mono<UserEntity> findByUsername(String username);

    Mono<UserEntity> findByEmail(String email);

    Mono<UserEntity> findByResetToken(String resetToken);

}