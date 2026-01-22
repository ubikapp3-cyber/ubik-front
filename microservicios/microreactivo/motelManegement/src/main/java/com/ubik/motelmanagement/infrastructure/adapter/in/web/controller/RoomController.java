package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.RoomUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateRoomRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.RoomResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateRoomRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.RoomDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para operaciones CRUD de Room
 * 
 * ENDPOINTS PÚBLICOS (sin autenticación):
 * - GET /api/rooms
 * - GET /api/rooms/{id}
 * - GET /api/rooms/motel/{motelId}
 * - GET /api/rooms/motel/{motelId}/available
 * 
 * ENDPOINTS PROTEGIDOS (requieren autenticación):
 * - POST /api/rooms
 * - PUT /api/rooms/{id}
 * - DELETE /api/rooms/{id}
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomUseCasePort roomUseCasePort;
    private final RoomDtoMapper roomDtoMapper;

    public RoomController(RoomUseCasePort roomUseCasePort, RoomDtoMapper roomDtoMapper) {
        this.roomUseCasePort = roomUseCasePort;
        this.roomDtoMapper = roomDtoMapper;
    }

    /**
     *  PÚBLICO - Obtiene todas las habitaciones
     * GET /api/rooms
     */
    @GetMapping
    public Flux<RoomResponse> getAllRooms() {
        return roomUseCasePort.getAllRooms()
                .map(roomDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene una habitación por ID
     * GET /api/rooms/{id}
     */
    @GetMapping("/{id}")
    public Mono<RoomResponse> getRoomById(@PathVariable Long id) {
        return roomUseCasePort.getRoomById(id)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene habitaciones por ID de motel
     * GET /api/rooms/motel/{motelId}
     */
    @GetMapping("/motel/{motelId}")
    public Flux<RoomResponse> getRoomsByMotelId(@PathVariable Long motelId) {
        return roomUseCasePort.getRoomsByMotelId(motelId)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * ÚBLICO - Obtiene habitaciones disponibles por ID de motel
     * GET /api/rooms/motel/{motelId}/available
     */
    @GetMapping("/motel/{motelId}/available")
    public Flux<RoomResponse> getAvailableRoomsByMotelId(@PathVariable Long motelId) {
        return roomUseCasePort.getAvailableRoomsByMotelId(motelId)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Crea una nueva habitación
     * POST /api/rooms
     * Requiere: Header X-User-Username y X-User-Role
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            ServerWebExchange exchange) {
        
        // Validar que el usuario esté autenticado
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        String role = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        
        if (username == null || role == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return Mono.just(request)
                .map(roomDtoMapper::toDomain)
                .flatMap(roomUseCasePort::createRoom)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Actualiza una habitación existente
     * PUT /api/rooms/{id}
     * Requiere: Header X-User-Username y X-User-Role
     */
    @PutMapping("/{id}")
    public Mono<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request,
            ServerWebExchange exchange) {
        
        // Validar autenticación
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return Mono.just(request)
                .map(roomDtoMapper::toDomain)
                .flatMap(room -> roomUseCasePort.updateRoom(id, room))
                .map(roomDtoMapper::toResponse);
    }

    /**
     * PROTEGIDO - Elimina una habitación
     * DELETE /api/rooms/{id}
     * Requiere: Header X-User-Username y X-User-Role
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteRoom(
            @PathVariable Long id,
            ServerWebExchange exchange) {
        
        // Validar autenticación
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");
        
        if (username == null) {
            return Mono.error(new RuntimeException("Usuario no autenticado"));
        }
        
        return roomUseCasePort.deleteRoom(id);
    }
}
