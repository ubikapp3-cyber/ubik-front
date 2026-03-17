package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.port.in.ReservationUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.ReservationResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.ReservationDtoMapper;

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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
    private final ReservationUseCasePort reservationUseCasePort;
    private final ReservationDtoMapper reservationDtoMapper;

    public RoomController(
            RoomUseCasePort roomUseCasePort, 
            RoomDtoMapper roomDtoMapper,
            ReservationUseCasePort reservationUseCasePort,
            ReservationDtoMapper reservationDtoMapper) {
        this.roomUseCasePort = roomUseCasePort;
        this.roomDtoMapper = roomDtoMapper;
        this.reservationUseCasePort = reservationUseCasePort;
        this.reservationDtoMapper = reservationDtoMapper;
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
     * PÚBLICO - Obtiene habitaciones disponibles por ID de motel
     * GET /api/rooms/motel/{motelId}/available
     */
    @GetMapping("/motel/{motelId}/available")
    public Flux<RoomResponse> getAvailableRoomsByMotelId(@PathVariable Long motelId) {
        return roomUseCasePort.getAvailableRoomsByMotelId(motelId)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Obtiene las reservas de una habitación para un día específico
     * GET /api/rooms/{id}/reservations
     */
    @GetMapping("/{id}/reservations")
    public Flux<ReservationResponse> getRoomReservations(
            @PathVariable Long id,
            @RequestParam(required = false) String date) {
        
        return reservationUseCasePort.getActiveReservationsByRoomId(id)
                .filter(res -> {
                    if (date == null || date.isBlank()) return true;
                    LocalDate targetDate = LocalDate.parse(date);
                    return !res.checkInDate().toLocalDate().isAfter(targetDate) && 
                           !res.checkOutDate().toLocalDate().isBefore(targetDate);
                })
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * PÚBLICO - Calcula la disponibilidad de una habitación en un rango de horas
     * GET /api/rooms/{id}/availability
     */
    @GetMapping("/{id}/availability")
    public Mono<Boolean> checkRoomTimeAvailability(
            @PathVariable Long id,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        
        LocalDate targetDate = LocalDate.parse(date);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        
        LocalDateTime checkIn = LocalDateTime.of(targetDate, start);
        LocalDateTime checkOut = LocalDateTime.of(targetDate, end);
        
        // Si el checkOut es antes o igual al checkIn (ej. 21:00 a 01:00), asumimos que termina al día siguiente
        if (!checkOut.isAfter(checkIn)) {
            checkOut = checkOut.plusDays(1);
        }

        return reservationUseCasePort.isRoomAvailable(id, checkIn, checkOut);
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
