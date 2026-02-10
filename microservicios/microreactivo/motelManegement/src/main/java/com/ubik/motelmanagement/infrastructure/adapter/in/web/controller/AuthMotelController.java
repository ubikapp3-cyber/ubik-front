package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;

/**
 * Controlador REST para operaciones autenticadas de Motel
 * 
 * ENDPOINTS PROTEGIDOS (requieren autenticación):
 * - GET /api/auth/motels/{userId} - Obtiene moteles del propietario autenticado
 * 
 * Este controlador maneja operaciones que requieren que el usuario esté autenticado
 * y que el ID del usuario en la URL coincida con el usuario autenticado.
 */
@RestController
@RequestMapping("/api/auth/motels")
public class AuthMotelController {

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;

    public AuthMotelController(MotelUseCasePort motelUseCasePort, MotelDtoMapper motelDtoMapper) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
    }

    /**
     * PROTEGIDO - Obtiene todos los moteles del propietario autenticado
     * GET /api/auth/motels/{userId}
     * 
     * Este endpoint verifica que:
     * 1. El usuario esté autenticado (headers X-User-Id)
     * 2. El userId en la URL coincida con el usuario autenticado
     * 3. Retorna solo los moteles donde propertyId == userId
     * 
     * @param userId ID del propietario (debe coincidir con el usuario autenticado)
     * @param exchange ServerWebExchange para obtener headers de autenticación
     * @return Flux con los moteles del propietario
     */
    @GetMapping("/{userId}")
    public Flux<MotelResponse> getMotelsByAuthenticatedUser(
            @PathVariable Long userId,
            ServerWebExchange exchange) {
        
        // 1. Obtener el ID del usuario autenticado desde los headers
        String authenticatedUserIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        
        // 2. Validar que el usuario esté autenticado
        if (authenticatedUserIdHeader == null || authenticatedUserIdHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, 
                    "Usuario no autenticado. Se requiere el header X-User-Id");
        }
        
        Long authenticatedUserId;
        try {
            authenticatedUserId = Long.parseLong(authenticatedUserIdHeader);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "El header X-User-Id debe ser un número válido");
        }
        
        // 3. Validar que el userId de la URL coincida con el usuario autenticado
        if (!authenticatedUserId.equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, 
                    "No tienes permiso para acceder a los moteles de otro usuario");
        }
        
        // 4. Retornar los moteles del propietario autenticado
        return motelUseCasePort.getMotelsByPropertyId(userId)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Obtiene los moteles del usuario autenticado sin pasar ID en URL
     * GET /api/auth/motels/my-motels
     * 
     * Alternativa más simple que obtiene automáticamente el userId del header
     * 
     * @param exchange ServerWebExchange para obtener headers de autenticación
     * @return Flux con los moteles del propietario autenticado
     */
    @GetMapping("/my-motels")
    public Flux<MotelResponse> getMyMotels(ServerWebExchange exchange) {
        
        // Obtener el ID del usuario autenticado desde los headers
        String authenticatedUserIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        
        // Validar que el usuario esté autenticado
        if (authenticatedUserIdHeader == null || authenticatedUserIdHeader.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, 
                    "Usuario no autenticado. Se requiere el header X-User-Id");
        }
        
        Long authenticatedUserId;
        try {
            authenticatedUserId = Long.parseLong(authenticatedUserIdHeader);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "El header X-User-Id debe ser un número válido");
        }
        
        // Retornar los moteles del propietario autenticado
        return motelUseCasePort.getMotelsByPropertyId(authenticatedUserId)
                .map(motelDtoMapper::toResponse);
    }
}