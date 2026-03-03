package com.ubik.usermanagement.application.port.out;

import io.jsonwebtoken.Claims;

import java.util.Map;

public interface JwtPort {

    String generateToken(String username, Long roleId, Long userId);

    Map<String, Object> extractClaims(String token);

    boolean isTokenValid(String token, String expectedUsername);
}
