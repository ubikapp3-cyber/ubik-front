package com.ubik.usermanagement.application.port.in;

import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UserProfileResponse;
import reactor.core.publisher.Mono;

public interface UserProfileUseCase {
    Mono<UserProfileResponse> getUserProfile(String username);
    Mono<UserProfileResponse> updateUserProfile(String username, UpdateUserRequest request);
    Mono<UserProfileResponse> getUserProfileById(Long id);
    Mono<Boolean> deleteUserProfile(String username);
}
