package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.MotelUseCasePort;
import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;  
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para operaciones CRUD de Motel
 * 
 * ENDPOINTS PÚBLICOS (sin autenticación):
 * - GET /api/motels
 * - GET /api/motels/{id}
 * - GET /api/motels/city/{city}
 * 
 * ENDPOINTS PROTEGIDOS (requieren autenticación):
 * - POST /api/motels
 * - PUT /api/motels/{id}
 * - DELETE /api/motels/{id}
 */
@RestController
@RequestMapping("/api/motels")
public class MotelController {

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;

    public MotelController(MotelUseCasePort motelUseCasePort, MotelDtoMapper motelDtoMapper) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
    }

    /**
     * PÚBLICO - Obtiene todos los moteles
     * GET /api/motels
     */
    @GetMapping
    public Flux<MotelResponse> getAllMotels() {
        return motelUseCasePort.getAllMotels()
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene un motel por ID
     * GET /api/motels/{id}
     */
    @GetMapping("/{id}")
    public Mono<MotelResponse> getMotelById(@PathVariable Long id) {
        return motelUseCasePort.getMotelById(id)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene moteles por ciudad
     * GET /api/motels/city/{city}
     */
    @GetMapping("/city/{city}")
    public Flux<MotelResponse> getMotelsByCity(@PathVariable String city) {
        return motelUseCasePort.getMotelsByCity(city)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Crea un nuevo motel
     * POST /api/motels
     * Requiere: Header X-User-Username y X-User-Role
     */
    @PostMapping
@ResponseStatus(HttpStatus.CREATED)
public Mono<MotelResponse> createMotel(
        @Valid @RequestBody CreateMotelRequest request,
        ServerWebExchange exchange) {
    
    String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
    String userIdHeader = exchange.getRequest().getHeaders().getFirst("X-User-Id");
    
    if (username == null || userIdHeader == null) {
        return Mono.error(new RuntimeException("Usuario no autenticado"));
    }
    
    Long userId;
    try {
        userId = Long.parseLong(userIdHeader);
    } catch (NumberFormatException e) {
        return Mono.error(new RuntimeException("Header X-User-Id inválido"));
    }
    
    // ✅ Asignar el userId del token como propertyId del motel
    final Long finalUserId = userId;
    return Mono.just(request)
            .map(motelDtoMapper::toDomain)
            .map(motel -> new Motel(
                    motel.id(),
                    motel.name(),
                    motel.address(),
                    motel.phoneNumber(),
                    motel.description(),
                    motel.city(),
                    finalUserId,          // ✅ propertyId = userId autenticado
                    motel.dateCreated(),
                    motel.imageUrls(),
                    motel.latitude(),
                    motel.longitude(),
                    motel.approvalStatus(),
                    motel.approvalDate(),
                    motel.approvedByUserId(),
                    motel.rejectionReason(),
                    motel.rues(),
                    motel.rnt(),
                    motel.ownerDocumentType(),
                    motel.ownerDocumentNumber(),
                    motel.ownerFullName(),
                    motel.legalRepresentativeName(),
                    motel.legalDocumentUrl()
            ))
            .flatMap(motelUseCasePort::createMotel)
            .map(motelDtoMapper::toResponse);
}

    /**
     * PROTEGIDO - Actualiza un motel existente
     * PUT /api/motels/{id}
     * Requiere: Header X-User-Username y X-User-Role
     */
    @PutMapping("/{id}")
    public Mono<MotelResponse> updateMotel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMotelRequest request,
            ServerWebExchange exchange) {
        
        // Opcional: Validar autenticación
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return Mono.just(request)
                .map(motelDtoMapper::toDomain)
                .flatMap(motel -> motelUseCasePort.updateMotel(id, motel))
                .map(motelDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Elimina un motel
     * DELETE /api/motels/{id}
     * Requiere: Header X-User-Username y X-User-Role
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMotel(
            @PathVariable Long id,
            ServerWebExchange exchange) {
        
        // Opcional: Validar autenticación
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return motelUseCasePort.deleteMotel(id);
    }
}