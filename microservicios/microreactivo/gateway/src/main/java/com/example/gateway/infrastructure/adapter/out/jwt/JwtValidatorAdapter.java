package com.example.gateway.infrastructure.adapter.out.jwt;

import com.example.gateway.domain.port.out.JwtValidatorPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Map;

@Component
public class JwtValidatorAdapter implements JwtValidatorPort {

    private final SecretKey key;

    public JwtValidatorAdapter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public Map<String, Object> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Map.copyOf(claims);
        } catch (JwtException e) {
            throw new IllegalArgumentException("Token inválido o expirado", e);
        }
    }
}
