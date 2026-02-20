package com.example.gateway.application.config;

import com.example.gateway.application.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String ROLE_ID_ADMIN =
            System.getenv().getOrDefault("ROLE_ID_ADMIN", "7392841056473829");

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationFilter jwtFilter) {

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/motels/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                        .pathMatchers("/api/admin/motels/**").hasAuthority("ROLE_" + ROLE_ID_ADMIN)
                        .pathMatchers(HttpMethod.GET, "/api/auth/motels/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/motels/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/motels/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/motels/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/rooms/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/rooms/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/rooms/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/services/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/services/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/services/**").authenticated()
                        .pathMatchers("/api/reservations/**").authenticated()
                        .pathMatchers("/api/user/**").authenticated()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Type",
                "X-User-Username", "X-User-Role", "X-User-Id"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}