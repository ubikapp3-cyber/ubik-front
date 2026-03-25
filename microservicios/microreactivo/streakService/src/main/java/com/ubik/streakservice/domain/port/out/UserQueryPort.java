package com.ubik.streakservice.domain.port.out;

import com.ubik.streakservice.domain.model.UserSummary;
import reactor.core.publisher.Mono;

public interface UserQueryPort {
    Mono<UserSummary> getUserSummary(Long userId);
}
