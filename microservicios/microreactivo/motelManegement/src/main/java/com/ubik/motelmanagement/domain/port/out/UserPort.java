package com.ubik.motelmanagement.domain.port.out;

import com.ubik.motelmanagement.domain.model.UserSummary;
import reactor.core.publisher.Mono;

public interface UserPort {
    Mono<UserSummary> getUserById(Long id);
}
