package com.example.gateway.domain.port.out;

import java.util.Map;

public interface JwtValidatorPort {
    
    /**
     * Validates a JWT token and extracts its claims.
     * 
     * @param token the JWT token to validate
     * @return a map containing the token claims
     * @throws IllegalArgumentException if the token is invalid or expired
     */
    Map<String, Object> validateToken(String token);
}
