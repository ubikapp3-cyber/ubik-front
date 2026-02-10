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

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationFilter jwtFilter) {
        
        return http
                // ========================================
                // CRÍTICO: CORS PRIMERO
                // ========================================
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Deshabilitar CSRF para APIs REST
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                // Deshabilitar autenticación básica y formularios
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                
                // Configurar reglas de autorización
                .authorizeExchange(exchanges -> exchanges
                        // ========================================
                        // CRÍTICO: Permitir OPTIONS sin autenticación
                        // (requerido para CORS preflight)
                        // ========================================
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // ========================================
                        // RUTAS COMPLETAMENTE PÚBLICAS (sin token)
                        // ========================================
                        
                        // Autenticación - público
                        .pathMatchers("/api/auth/**").permitAll()
                        
                        // Actuator - público
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Moteles - SOLO lectura es pública (SOLO APROBADOS)
                        .pathMatchers(HttpMethod.GET, "/api/motels/**").permitAll()
                        
                        // Habitaciones - SOLO lectura es pública
                        .pathMatchers(HttpMethod.GET, "/api/rooms/**").permitAll()
                        
                        // Servicios - SOLO lectura es pública
                        .pathMatchers(HttpMethod.GET, "/api/services/**").permitAll()
                        
                        // ========================================
                        // RUTAS DE ADMINISTRACIÓN (SOLO ROLE = 1)
                        // ========================================
                        
                        // Gestión de aprobación de moteles - SOLO ADMIN
                        .pathMatchers("/api/admin/motels/**").hasAuthority("ROLE_1")
                        
                        // ========================================
                        // RUTAS AUTENTICADAS - Moteles del propietario
                        // ========================================
                        

                        .pathMatchers(HttpMethod.GET, "/api/auth/motels/**").authenticated()
                        
                        // ========================================
                        // RUTAS QUE REQUIEREN AUTENTICACIÓN
                        // ========================================
                        
                        // Cualquier POST, PUT, DELETE en motels/rooms/services
                        .pathMatchers(HttpMethod.POST, "/api/motels/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/motels/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/motels/**").authenticated()
                        
                        .pathMatchers(HttpMethod.POST, "/api/rooms/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/rooms/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/rooms/**").authenticated()
                        
                        .pathMatchers(HttpMethod.POST, "/api/services/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/services/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/api/services/**").authenticated()
                        
                        // Reservas - siempre requieren autenticación
                        .pathMatchers("/api/reservations/**").authenticated()
                        
                        // Perfil de usuario - requiere autenticación
                        .pathMatchers("/api/user/**").authenticated()
                        
                        // Todo lo demás requiere autenticación
                        .anyExchange().authenticated()
                )
                
                // Activar el filtro JWT DESPUÉS de CORS
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                
                .build();
    }
    
    /**
     * Configuración de CORS integrada en SecurityConfig
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir cualquier origen (desarrollo)
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Para producción, usar orígenes específicos:
        // configuration.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:4200",
        //     "https://ubik-back.duckdns.org",
        //     "https://tu-dominio-frontend.com"
        // ));
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));
        
        // Headers permitidos
        configuration.setAllowedHeaders(List.of("*"));
        
        // Headers expuestos
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-User-Username",
            "X-User-Role",
            "X-User-Id"
        ));
        
        // Permitir credenciales
        configuration.setAllowCredentials(true);
        
        // Cache de preflight
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}