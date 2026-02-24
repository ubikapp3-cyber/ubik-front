package com.ubik.motelmanagement.infrastructure.adapter.out.persistence;

import com.ubik.motelmanagement.domain.model.Reservation;
import com.ubik.motelmanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper.ReservationMapper;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.repository.ReservationR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Adaptador de persistencia para Reservation
 * Implementa métodos para confirmationCode
 */
@Component
public class ReservationPersistenceAdapter implements ReservationRepositoryPort {

    private final ReservationR2dbcRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public ReservationPersistenceAdapter(
            ReservationR2dbcRepository reservationRepository,
            ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public Mono<Reservation> save(Reservation reservation) {
        return Mono.just(reservation)
                .map(reservationMapper::toEntity)
                .flatMap(reservationRepository::save)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Mono<Reservation> update(Reservation reservation) {
        return save(reservation);
    }

    @Override
    public Mono<Reservation> findById(Long id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findAll() {
        return reservationRepository.findAll()
                .map(reservationMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return reservationRepository.existsById(id);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return reservationRepository.deleteById(id);
    }

    @Override
    public Flux<Reservation> findByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findByUserId(Long userId) {
        return reservationRepository.findByUserId(userId)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findActiveReservationsByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId)
                .map(reservationMapper::toDomain)
                .filter(Reservation::isActive);
    }

    @Override
    public Flux<Reservation> findByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findAll()
                .map(reservationMapper::toDomain)
                .filter(reservation -> reservation.status() == status);
    }

    @Override
    public Flux<Reservation> findOverlappingReservations(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return reservationRepository.findConflictingReservations(roomId, checkIn, checkOut)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findAll()
                .map(reservationMapper::toDomain)
                .filter(reservation -> {
                    LocalDate checkIn = reservation.checkInDate().toLocalDate();
                    LocalDate checkOut = reservation.checkOutDate().toLocalDate();
                    return !(checkOut.isBefore(startDate) || checkIn.isAfter(endDate));
                });
    }

    @Override
    public Mono<Boolean> hasConflictingReservations(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeReservationId) {
        if (excludeReservationId != null) {
            return reservationRepository.findConflictingReservationsExcluding(
                    roomId,
                    checkIn.atStartOfDay(),
                    checkOut.atTime(23, 59, 59),
                    excludeReservationId
            ).hasElements();
        } else {
            return reservationRepository.findConflictingReservations(
                    roomId,
                    checkIn.atStartOfDay(),
                    checkOut.atTime(23, 59, 59)
            ).hasElements();
        }
    }

    @Override
    public Mono<Boolean> existsActiveReservationWithCode(String confirmationCode) {
        return reservationRepository.existsActiveReservationWithCode(confirmationCode);
    }

    @Override
    public Mono<Reservation> findByConfirmationCode(String confirmationCode) {
        return reservationRepository.findByConfirmationCode(confirmationCode)
                .map(reservationMapper::toDomain);
    }
}