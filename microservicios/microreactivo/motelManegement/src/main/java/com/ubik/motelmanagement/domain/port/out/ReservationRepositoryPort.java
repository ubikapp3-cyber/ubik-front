package com.ubik.motelmanagement.domain.port.out;

import com.ubik.motelmanagement.domain.model.Reservation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Puerto de salida para persistencia de Reservation
 */
public interface ReservationRepositoryPort {

    Mono<Reservation> save(Reservation reservation);

    Mono<Reservation> update(Reservation reservation);

    Mono<Reservation> findById(Long id);

    Flux<Reservation> findAll();

    Mono<Boolean> existsById(Long id);

    Mono<Void> deleteById(Long id);

    // Métodos de búsqueda específicos
    Flux<Reservation> findByRoomId(Long roomId);

    Flux<Reservation> findByUserId(Long userId);

    Flux<Reservation> findActiveReservationsByRoomId(Long roomId);

    Flux<Reservation> findByStatus(Reservation.ReservationStatus status);

    Flux<Reservation> findOverlappingReservations(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut);

    Flux<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate);

    Mono<Boolean> hasConflictingReservations(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeReservationId);

    Mono<Boolean> existsActiveReservationWithCode(String confirmationCode);

    Mono<Reservation> findByConfirmationCode(String confirmationCode);
}