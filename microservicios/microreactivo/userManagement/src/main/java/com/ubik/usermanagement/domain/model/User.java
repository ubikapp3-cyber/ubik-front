package com.ubik.usermanagement.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio User
 * 
 * @param id Identificador único
 * @param username Nombre de usuario único
 * @param password Contraseña encriptada
 * @param email Correo electrónico
 * @param phoneNumber Número telefónico
 * @param createdAt Fecha de registro
 * @param anonymous Indica si el usuario es anónimo
 * @param roleId ID del rol asignado
 * @param resetToken Token para reseteo de contraseña
 * @param resetTokenExpiry Expiración del token de reseteo
 * @param longitude Coordenada de longitud geográfica (-180 a 180)
 * @param latitude Coordenada de latitud geográfica (-90 a 90)
 * @param birthDate Fecha de nacimiento del usuario
 */
public record User(
        Long id,
        String username,
        String password,
        String email,
        String phoneNumber,
        LocalDateTime createdAt,
        boolean anonymous,
        Long roleId,  // era Integer
        String resetToken,
        LocalDateTime resetTokenExpiry,
        BigDecimal longitude,
        BigDecimal latitude,
        LocalDate birthDate
) {
    /**
     * Validación de coordenadas geográficas
     */
    public User {
        if (longitude != null && (longitude.compareTo(new BigDecimal("-180")) < 0 
            || longitude.compareTo(new BigDecimal("180")) > 0)) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        
        if (latitude != null && (latitude.compareTo(new BigDecimal("-90")) < 0 
            || latitude.compareTo(new BigDecimal("90")) > 0)) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Birth date cannot be in the future");
        }
    }
    
    /**
     * Calcula la edad del usuario basado en la fecha de nacimiento
     * @return edad en años, o null si no hay fecha de nacimiento
     */
    public Integer calculateAge() {
        if (birthDate == null) return null;
        return LocalDate.now().getYear() - birthDate.getYear();
    }
    
    /**
     * Verifica si el usuario tiene ubicación definida
     */
    public boolean hasLocation() {
        return longitude != null && latitude != null;
    }
}