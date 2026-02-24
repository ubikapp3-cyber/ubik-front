package com.ubik.usermanagement.application.port.out;

import com.ubik.usermanagement.domain.model.User;
import reactor.core.publisher.Mono;

public interface UserRepositoryPort {
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Mono<User> findByResetToken(String resetToken);
    Mono<User> save(User user);
    Mono<User> findById(Long id);

    Mono<User> update(User user);

    Mono<Void> deleteById(Long id);
}
