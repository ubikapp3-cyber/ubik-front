package com.ubik.paymentservice.domain.port.out;

import com.ubik.paymentservice.infrastructure.adapter.out.usermanagement.dto.UserProfileDto;
import reactor.core.publisher.Mono;

public interface UserManagementPort {
    Mono<UserProfileDto> getUserProfile(Long userId);
}
