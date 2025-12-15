package com.example.gateway.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web. cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Permitir TODOS los orígenes
        corsConfig.addAllowedOriginPattern("*");
        
        // Permitir TODOS los métodos
        corsConfig.setAllowedMethods(Arrays. asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // Permitir TODOS los headers
        corsConfig.addAllowedHeader("*");
        
        // Permitir credenciales
        corsConfig.setAllowCredentials(true);
        
        // Exponer todos los headers
        corsConfig.addExposedHeader("*");
        
        // Max age
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}