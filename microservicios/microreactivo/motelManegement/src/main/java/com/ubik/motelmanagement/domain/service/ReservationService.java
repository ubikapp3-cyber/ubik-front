package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.Reservation;
import com.ubik.motelmanagement.domain.port.in.ReservationUseCasePort;
import com.ubik.motelmanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.RoomRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Servicio de dominio que implementa los casos de uso de Reservation
 * Contiene la lógica de negocio para gestión de reservas
 */
@Service
public class ReservationService implements ReservationUseCasePort {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final RoomRepositoryPort roomRepositoryPort;

    public ReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            RoomRepositoryPort roomRepositoryPort
    ) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.roomRepositoryPort = roomRepositoryPort;
    }

    @Override
    public Mono<Reservation> createReservation(Reservation reservation) {
        return validateReservation(reservation)
                .then(roomRepositoryPort.existsById(reservation.roomId()))
                .flatMap(roomExists -> {
                    if (!roomExists) {
                        return Mono.error(new RuntimeException("Habitación no encontrada con ID: " + reservation.roomId()));
                    }
                    return isRoomAvailable(reservation.roomId(), reservation.checkInDate(), reservation.checkOutDate());
                })
                .flatMap(isAvailable -> {
                    if (!isAvailable) {
                        return Mono.error(new IllegalArgumentException(
                                "La habitación no está disponible para las fechas seleccionadas"));
                    }
                    return reservationRepositoryPort.save(reservation);
                });
    }

    @Override
    public Mono<Reservation> getReservationById(Long id) {
        return reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada con ID: " + id)));
    }

    @Override
    public Flux<Reservation> getAllReservations() {
        return reservationRepositoryPort.findAll();
    }

    @Override
    public Flux<Reservation> getReservationsByRoomId(Long roomId) {
        return reservationRepositoryPort.findByRoomId(roomId);
    }

    @Override
    public Flux<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepositoryPort.findByUserId(userId);
    }

    @Override
    public Flux<Reservation> getActiveReservationsByRoomId(Long roomId) {
        return reservationRepositoryPort.findActiveReservationsByRoomId(roomId);
    }

    @Override
    public Flux<Reservation> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepositoryPort.findByStatus(status);
    }

    @Override
    public Mono<Boolean> isRoomAvailable(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return reservationRepositoryPort.findOverlappingReservations(roomId, checkIn, checkOut)
                .filter(Reservation::isActive)
                .hasElements()
                .map(hasOverlapping -> !hasOverlapping); // Si no hay solapamientos, está disponible
    }

    @Override
    public Mono<Reservation> updateReservation(Long id, Reservation reservation) {
        return reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada con ID: " + id)))
                .flatMap(existingReservation -> {
                    if (!existingReservation.canBeCancelled()) {
                        return Mono.error(new IllegalArgumentException(
                                "La reserva no puede ser modificada en su estado actual: " + existingReservation.status()));
                    }

                    // Verificar disponibilidad si se cambian las fechas
                    boolean datesChanged = !existingReservation.checkInDate().equals(reservation.checkInDate()) ||
                            !existingReservation.checkOutDate().equals(reservation.checkOutDate());

                    if (datesChanged) {
                        return isRoomAvailable(existingReservation.roomId(),
                                reservation.checkInDate(),
                                reservation.checkOutDate())
                                .flatMap(isAvailable -> {
                                    if (!isAvailable) {
                                        return Mono.error(new IllegalArgumentException(
                                                "La habitación no está disponible para las nuevas fechas"));
                                    }
                                    Reservation updatedReservation = new Reservation(
                                            id,
                                            existingReservation.roomId(),
                                            existingReservation.userId(),
                                            reservation.checkInDate(),
                                            reservation.checkOutDate(),
                                            existingReservation.status(),
                                            reservation.totalPrice(),
                                            reservation.specialRequests(),
                                            existingReservation.createdAt(),
                                            LocalDateTime.now()
                                    );
                                    return validateReservation(updatedReservation)
                                            .then(reservationRepositoryPort.update(updatedReservation));
                                });
                    } else {
                        Reservation updatedReservation = new Reservation(
                                id,
                                existingReservation.roomId(),
                                existingReservation.userId(),
                                reservation.checkInDate(),
                                reservation.checkOutDate(),
                                existingReservation.status(),
                                reservation.totalPrice(),
                                reservation.specialRequests(),
                                existingReservation.createdAt(),
                                LocalDateTime.now()
                        );
                        return validateReservation(updatedReservation)
                                .then(reservationRepositoryPort.update(updatedReservation));
                    }
                });
    }

    @Override
    public Mono<Reservation> confirmReservation(Long id) {
        return reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada con ID: " + id)))
                .flatMap(reservation -> {
                    if (!reservation.canBeConfirmed()) {
                        return Mono.error(new IllegalArgumentException(
                                "La reserva no puede ser confirmada en su estado actual: " + reservation.status()));
                    }
                    Reservation confirmedReservation = reservation.withStatus(Reservation.ReservationStatus.CONFIRMED);
                    return reservationRepositoryPort.update(confirmedReservation);
                });
    }

    @Override
    public Mono<Reservation> cancelReservation(Long id) {
        return reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada con ID: " + id)))
                .flatMap(reservation -> {
                    if (!reservation.canBeCancelled()) {
                        return Mono.error(new IllegalArgumentException(
                                "La reserva no puede ser cancelada en su estado actual: " + reservation.status()));
                    }
                    Reservation cancelledReservation = reservation.withStatus(Reservation.ReservationStatus.CANCELLED);
                    return reservationRepositoryPort.update(cancelledReservation);
                });
    }

    @Override
    public Mono<Reservation> checkIn(Long id) {
        return reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada con ID: " + id)))
                .flatMap(reservation -> {
                    if (!reservation.canCheckIn()) {
                        return Mono.error(new IllegalArgumentException(
                                "No se puede hacer check-in en el estado actual: " + reservation.status()));
                    }
                    Reservation checkedInReservation = reservation.withStatus(Reservation.ReservationStatus.CHECKED_IN);
                    return reservationRepositoryPort.update(checkedInReservation);
                });
    }

    @Override
    public Mono<Reservation> checkOut(Long id) {
        return reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada con ID: " + id)))
                .flatMap(reservation -> {
                    if (!reservation.canCheckOut()) {
                        return Mono.error(new IllegalArgumentException(
                                "No se puede hacer check-out en el estado actual: " + reservation.status()));
                    }
                    Reservation checkedOutReservation = reservation.withStatus(Reservation.ReservationStatus.CHECKED_OUT);
                    return reservationRepositoryPort.update(checkedOutReservation);
                });
    }

    @Override
    public Mono<Void> deleteReservation(Long id) {
        return reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada con ID: " + id)))
                .flatMap(reservation -> {
                    if (reservation.status() != Reservation.ReservationStatus.CANCELLED) {
                        return Mono.error(new IllegalArgumentException(
                                "Solo se pueden eliminar reservas canceladas"));
                    }
                    return reservationRepositoryPort.deleteById(id);
                });
    }

    /**
     * Validaciones de negocio para una reserva
     */
    private Mono<Void> validateReservation(Reservation reservation) {
        if (reservation.roomId() == null) {
            return Mono.error(new IllegalArgumentException("El ID de la habitación es requerido"));
        }
        if (reservation.userId() == null) {
            return Mono.error(new IllegalArgumentException("El ID del usuario es requerido"));
        }
        if (reservation.checkInDate() == null) {
            return Mono.error(new IllegalArgumentException("La fecha de check-in es requerida"));
        }
        if (reservation.checkOutDate() == null) {
            return Mono.error(new IllegalArgumentException("La fecha de check-out es requerida"));
        }
        if (reservation.checkInDate().isAfter(reservation.checkOutDate())) {
            return Mono.error(new IllegalArgumentException(
                    "La fecha de check-in debe ser anterior a la fecha de check-out"));
        }
        if (reservation.checkInDate().isBefore(LocalDateTime.now())) {
            return Mono.error(new IllegalArgumentException(
                    "La fecha de check-in no puede ser en el pasado"));
        }
        if (reservation.totalPrice() == null || reservation.totalPrice() <= 0) {
            return Mono.error(new IllegalArgumentException("El precio total debe ser mayor que cero"));
        }
        return Mono.empty();
    }
}