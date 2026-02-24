package com.ubik.motelmanagement.infrastructure.adapter.in.web.controller;

import com.ubik.motelmanagement.domain.model.Reservation;
import com.ubik.motelmanagement.domain.port.in.ReservationUseCasePort;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateReservationRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.ReservationResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateReservationRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper.ReservationDtoMapper;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.security.AuthContextResolver;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationUseCasePort reservationUseCasePort;
    private final ReservationDtoMapper reservationDtoMapper;
    private final AuthContextResolver authContextResolver;

    public ReservationController(
            ReservationUseCasePort reservationUseCasePort,
            ReservationDtoMapper reservationDtoMapper,
            AuthContextResolver authContextResolver) {
        this.reservationUseCasePort = reservationUseCasePort;
        this.reservationDtoMapper = reservationDtoMapper;
        this.authContextResolver = authContextResolver;
    }

    /**
     * POST /api/reservations
     * Crea una nueva reserva asignando el userId desde el usuario autenticado.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request,
            ServerWebExchange exchange) {

        return authContextResolver.resolveUserId(exchange)
                .flatMap(userId -> {
                    Reservation reservation = new Reservation(
                            null,
                            request.roomId(),
                            userId,                      // userId resuelto desde BD
                            request.checkInDate(),
                            request.checkOutDate(),
                            Reservation.ReservationStatus.PENDING,
                            request.totalPrice(),
                            request.specialRequests(),
                            null,                        // confirmationCode se genera en el servicio
                            null,
                            null
                    );
                    return reservationUseCasePort.createReservation(reservation);
                })
                .map(reservationDtoMapper::toResponse);
    }

    @GetMapping("/{id}")
    public Mono<ReservationResponse> getReservationById(@PathVariable Long id) {
        return reservationUseCasePort.getReservationById(id)
                .map(reservationDtoMapper::toResponse);
    }

    @GetMapping
    public Flux<ReservationResponse> getAllReservations() {
        return reservationUseCasePort.getAllReservations()
                .map(reservationDtoMapper::toResponse);
    }

    @GetMapping("/room/{roomId}")
    public Flux<ReservationResponse> getReservationsByRoomId(@PathVariable Long roomId) {
        return reservationUseCasePort.getReservationsByRoomId(roomId)
                .map(reservationDtoMapper::toResponse);
    }

    @GetMapping("/user/{userId}")
    public Flux<ReservationResponse> getReservationsByUserId(@PathVariable Long userId) {
        return reservationUseCasePort.getReservationsByUserId(userId)
                .map(reservationDtoMapper::toResponse);
    }

    @GetMapping("/room/{roomId}/active")
    public Flux<ReservationResponse> getActiveReservationsByRoomId(@PathVariable Long roomId) {
        return reservationUseCasePort.getActiveReservationsByRoomId(roomId)
                .map(reservationDtoMapper::toResponse);
    }

    @GetMapping("/status/{status}")
    public Flux<ReservationResponse> getReservationsByStatus(@PathVariable String status) {
        try {
            Reservation.ReservationStatus reservationStatus =
                    Reservation.ReservationStatus.valueOf(status.toUpperCase());
            return reservationUseCasePort.getReservationsByStatus(reservationStatus)
                    .map(reservationDtoMapper::toResponse);
        } catch (IllegalArgumentException e) {
            return Flux.error(new IllegalArgumentException("Estado inválido: " + status));
        }
    }

    @GetMapping("/room/{roomId}/available")
    public Mono<Boolean> checkRoomAvailability(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut) {
        return reservationUseCasePort.isRoomAvailable(roomId, checkIn, checkOut);
    }

    @PutMapping("/{id}")
    public Mono<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequest request) {
        return Mono.just(request)
                .map(reservationDtoMapper::toDomain)
                .flatMap(reservation -> reservationUseCasePort.updateReservation(id, reservation))
                .map(reservationDtoMapper::toResponse);
    }

    @PatchMapping("/{id}/confirm")
    public Mono<ReservationResponse> confirmReservation(@PathVariable Long id) {
        return reservationUseCasePort.confirmReservation(id)
                .map(reservationDtoMapper::toResponse);
    }

    @PatchMapping("/{id}/cancel")
    public Mono<ReservationResponse> cancelReservation(@PathVariable Long id) {
        return reservationUseCasePort.cancelReservation(id)
                .map(reservationDtoMapper::toResponse);
    }

    @PatchMapping("/{id}/checkin")
    public Mono<ReservationResponse> checkIn(@PathVariable Long id) {
        return reservationUseCasePort.checkIn(id)
                .map(reservationDtoMapper::toResponse);
    }

    @PatchMapping("/{id}/checkout")
    public Mono<ReservationResponse> checkOut(@PathVariable Long id) {
        return reservationUseCasePort.checkOut(id)
                .map(reservationDtoMapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteReservation(@PathVariable Long id) {
        return reservationUseCasePort.deleteReservation(id);
    }
}