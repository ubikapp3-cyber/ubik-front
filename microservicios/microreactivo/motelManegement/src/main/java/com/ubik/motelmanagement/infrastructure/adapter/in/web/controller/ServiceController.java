package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.ServiceUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateServiceRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.ServiceResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateServiceRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.ServiceDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para operaciones CRUD de Service
 * 
 * ENDPOINTS PÚBLICOS (sin autenticación):
 * - GET /api/services
 * - GET /api/services/{id}
 * - GET /api/services/name/{name}
 * - GET /api/services/room/{roomId}
 * 
 * ENDPOINTS PROTEGIDOS (requieren autenticación):
 * - POST /api/services
 * - PUT /api/services/{id}
 * - DELETE /api/services/{id}
 * - POST /api/services/room/{roomId}/service/{serviceId}
 * - DELETE /api/services/room/{roomId}/service/{serviceId}
 */
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceUseCasePort serviceUseCasePort;
    private final ServiceDtoMapper serviceDtoMapper;

    public ServiceController(ServiceUseCasePort serviceUseCasePort, ServiceDtoMapper serviceDtoMapper) {
        this.serviceUseCasePort = serviceUseCasePort;
        this.serviceDtoMapper = serviceDtoMapper;
    }

    /**
     * PÚBLICO - Obtiene todos los servicios
     * GET /api/services
     */
    @GetMapping
    public Flux<ServiceResponse> getAllServices() {
        return serviceUseCasePort.getAllServices()
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene un servicio por ID
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    public Mono<ServiceResponse> getServiceById(@PathVariable Long id) {
        return serviceUseCasePort.getServiceById(id)
                .map(serviceDtoMapper::toResponse);
    }

    /**
     *  PÚBLICO - Obtiene un servicio por nombre
     * GET /api/services/name/{name}
     */
    @GetMapping("/name/{name}")
    public Mono<ServiceResponse> getServiceByName(@PathVariable String name) {
        return serviceUseCasePort.getServiceByName(name)
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene los IDs de servicios de una habitación
     * GET /api/services/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    public Flux<Long> getServiceIdsByRoomId(@PathVariable Long roomId) {
        return serviceUseCasePort.getServiceIdsByRoomId(roomId);
    }

    /**
     * PROTEGIDO - Crea un nuevo servicio
     * POST /api/services
     * Requiere: Header X-User-Username y X-User-Role
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ServiceResponse> createService(
            @Valid @RequestBody CreateServiceRequest request,
            ServerWebExchange exchange) {
        
        // Validar que el usuario esté autenticado
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        
        if (username == null || role == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return Mono.just(request)
                .map(serviceDtoMapper::toDomain)
                .flatMap(serviceUseCasePort::createService)
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Actualiza un servicio existente
     * PUT /api/services/{id}
     * Requiere: Header X-User-Username y X-User-Role
     */
    @PutMapping("/{id}")
    public Mono<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceRequest request,
            ServerWebExchange exchange) {
        
        // Validar autenticación
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return Mono.just(request)
                .map(serviceDtoMapper::toDomain)
                .flatMap(service -> serviceUseCasePort.updateService(id, service))
                .map(serviceDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Elimina un servicio
     * DELETE /api/services/{id}
     * Requiere: Header X-User-Username y X-User-Role
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteService(
            @PathVariable Long id,
            ServerWebExchange exchange) {
        
        // Validar autenticación
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return serviceUseCasePort.deleteService(id);
    }

    /**
     * PROTEGIDO - Asocia un servicio a una habitación
     * POST /api/services/room/{roomId}/service/{serviceId}
     * Requiere: Header X-User-Username y X-User-Role
     */
    @PostMapping("/room/{roomId}/service/{serviceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> addServiceToRoom(
            @PathVariable Long roomId,
            @PathVariable Long serviceId,
            ServerWebExchange exchange) {
        
        // Validar autenticación
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return serviceUseCasePort.addServiceToRoom(roomId, serviceId);
    }

    /**
     * PROTEGIDO - Elimina un servicio de una habitación
     * DELETE /api/services/room/{roomId}/service/{serviceId}
     * Requiere: Header X-User-Username y X-User-Role
     */
    @DeleteMapping("/room/{roomId}/service/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeServiceFromRoom(
            @PathVariable Long roomId,
            @PathVariable Long serviceId,
            ServerWebExchange exchange) {
        
        // Validar autenticación
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return serviceUseCasePort.removeServiceFromRoom(roomId, serviceId);
    }
}
