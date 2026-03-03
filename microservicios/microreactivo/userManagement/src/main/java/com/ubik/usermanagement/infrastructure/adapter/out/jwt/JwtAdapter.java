package com.ubik.usermanagement.infrastructure.adapter.out.jwt;

import com.ubik.usermanagement.application.port.out.JwtPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtAdapter implements JwtPort {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtAdapter(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
    }

    @Override
    public String generateToken(String username, Long roleId, Long userId) {
        return Jwts.builder()
                .subject(username)
                .claim("role", roleId)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    @Override
    public Map<String, Object> extractClaims(String token) {
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

    @Override
    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Map<String, Object> claims = extractClaims(token);

            String username = (String) claims.get("sub"); // 'sub' = subject en JWT
            Long expSeconds = ((Number) claims.get("exp")).longValue();
            Date expiration = new Date(expSeconds * 1000);

            return username != null &&
                    username.equals(expectedUsername) &&
                    expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
