package com.ubik.usermanagement.application.port.out;

import reactor.core.publisher.Mono;

public interface NotificationPort {
    Mono<Void> sendPasswordRecoveryEmail(String email, String username, String token);
    Mono<Void> sendRegisterEmail(String email, String username);
}
