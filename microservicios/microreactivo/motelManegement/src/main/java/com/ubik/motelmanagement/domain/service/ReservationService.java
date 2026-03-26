package com.ubik.motelmanagement.domain.service;

import com.ubik.motelmanagement.domain.model.OwnerDashboardSummary;
import com.ubik.motelmanagement.domain.model.Reservation;
import com.ubik.motelmanagement.domain.model.RoomStatusBoardResponse;
import com.ubik.motelmanagement.domain.port.in.ReservationUseCasePort;
import com.ubik.motelmanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.motelmanagement.domain.port.out.RoomRepositoryPort;

import com.ubik.motelmanagement.infrastructure.service.ConfirmationCodeService;
import com.ubik.motelmanagement.infrastructure.client.StreakRecalcTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Service
public class ReservationService implements ReservationUseCasePort {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final RoomRepositoryPort roomRepositoryPort;
    private final ConfirmationCodeService confirmationCodeService;
    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;
    private final StreakRecalcTrigger streakRecalcTrigger;

    // Bus de eventos para tiempo real
    private final Sinks.Many<Reservation> reservationSink = Sinks.many().multicast().onBackpressureBuffer();

    public ReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            RoomRepositoryPort roomRepositoryPort,
            ConfirmationCodeService confirmationCodeService,
            DatabaseClient databaseClient,
            TransactionalOperator transactionalOperator,
            StreakRecalcTrigger streakRecalcTrigger
    ) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.roomRepositoryPort = roomRepositoryPort;
        this.confirmationCodeService = confirmationCodeService;
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.streakRecalcTrigger = streakRecalcTrigger;
    }

    @Override
    public Mono<Reservation> createReservation(Reservation reservation) {
        log.info("Creando nueva reserva para habitación {}", reservation.roomId());

        return transactionalOperator.transactional(
            confirmationCodeService.generateCode()
                .flatMap(confirmationCode -> {
                    Reservation reservationWithCode = new Reservation(
                            null,
                            reservation.roomId(),
                            reservation.userId(),
                            reservation.checkInDate(),
                            reservation.checkOutDate(),
                            Reservation.ReservationStatus.PENDING,
                            reservation.totalPrice(),
                            reservation.specialRequests(),
                            confirmationCode,
                            null, // null para que la BD use DEFAULT ubik_now()
                            null  // null para que la BD use DEFAULT ubik_now()
                    );

                    return setClientTimeInSession()
                            .then(validateReservation(reservationWithCode))
                            .then(roomRepositoryPort.existsById(reservation.roomId()))
                            .flatMap(roomExists -> {
                                if (!roomExists) {
                                    return Mono.error(new RuntimeException("Habitación no encontrada"));
                                }
                                return isRoomAvailable(reservation.roomId(), reservation.checkInDate(), reservation.checkOutDate());
                            })
                            .flatMap(isAvailable -> {
                                if (!isAvailable) {
                                    return Mono.error(new IllegalArgumentException("No disponible"));
                                }
                                return reservationRepositoryPort.save(reservationWithCode);
                            });
                })
                .doOnNext(saved -> reservationSink.tryEmitNext(saved))
                .flatMap(savedReservation ->
                    streakRecalcTrigger.triggerRecalculate(savedReservation.userId())
                        .onErrorResume(e -> {
                            log.warn("No se pudo recalcular streak para userId={}: {}",
                                     savedReservation.userId(), e.getMessage());
                            return Mono.empty();
                        })
                        .thenReturn(savedReservation)
                )
        );
    }

    private Mono<Void> setClientTimeInSession() {
        return Mono.deferContextual(ctx -> {
            String clientTime = ctx.getOrDefault("X-Client-Time", null);
            if (clientTime != null && !clientTime.isBlank()) {
                log.info("Estableciendo ubik.client_time en sesión DB: {}", clientTime);
                return databaseClient.sql("SELECT set_config('ubik.client_time', :time, true)")
                        .bind("time", clientTime)
                        .then();
            }
            log.debug("No hay client_time en el contexto reactivo para esta operación.");
            return Mono.empty();
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
        return transactionalOperator.transactional(
            reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada con ID: " + id)))
                .flatMap(existingReservation -> {
                    if (!existingReservation.canBeCancelled()) {
                        return Mono.error(new IllegalArgumentException(
                                "La reserva no puede ser modificada en su estado actual: " + existingReservation.status()));
                    }

                    // Verificar disponibilidad si se cambian las fechas
                    boolean datesChanged = !existingReservation.checkInDate().equals(reservation.checkInDate()) ||
                            !existingReservation.checkOutDate().equals(reservation.checkOutDate());

                    Reservation updatedReservation = new Reservation(
                            id,
                            existingReservation.roomId(),
                            existingReservation.userId(),
                            reservation.checkInDate(),
                            reservation.checkOutDate(),
                            existingReservation.status(),
                            reservation.totalPrice(),
                            reservation.specialRequests(),
                            existingReservation.confirmationCode(),
                            existingReservation.createdAt(),
                            null // Dejar que la BD use ubik_now() via trigger
                    );

                    Mono<Reservation> updateMono = setClientTimeInSession()
                            .then(validateReservation(updatedReservation))
                            .then(reservationRepositoryPort.update(updatedReservation));

                    if (datesChanged) {
                        return isRoomAvailable(existingReservation.roomId(),
                                reservation.checkInDate(),
                                reservation.checkOutDate())
                                .flatMap(isAvailable -> {
                                    if (!isAvailable) {
                                        return Mono.error(new IllegalArgumentException(
                                                "La habitación no está disponible para las nuevas fechas"));
                                    }
                                    return updateMono;
                                });
                    } else {
                        return updateMono;
                    }
                })
        );
    }

    @Override
    public Mono<Reservation> confirmReservation(Long id) {
        return transactionalOperator.transactional(
            reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")))
                .flatMap(reservation -> {
                    if (!reservation.canBeConfirmed()) {
                        return Mono.error(new IllegalArgumentException("No se puede confirmar"));
                    }
                    Reservation confirmedReservation = reservation.withStatus(Reservation.ReservationStatus.CONFIRMED);
                    return setClientTimeInSession()
                            .then(reservationRepositoryPort.update(confirmedReservation));
                })
                .doOnNext(reservationSink::tryEmitNext)
        );
    }

    @Override
    public Mono<Reservation> cancelReservation(Long id) {
        return transactionalOperator.transactional(
            reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")))
                .flatMap(reservation -> {
                    if (!reservation.canBeCancelled()) {
                        return Mono.error(new IllegalArgumentException("No se puede cancelar"));
                    }
                    Reservation cancelledReservation = reservation.withStatus(Reservation.ReservationStatus.CANCELLED);
                    return setClientTimeInSession()
                            .then(reservationRepositoryPort.update(cancelledReservation));
                })
                .doOnNext(reservationSink::tryEmitNext)
        );
    }

    @Override
    public Mono<Reservation> checkIn(Long id) {
        return transactionalOperator.transactional(
            reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")))
                .flatMap(reservation -> {
                    if (!reservation.canCheckIn()) {
                        return Mono.error(new IllegalArgumentException("No se puede hacer check-in"));
                    }
                    Reservation checkedInReservation = reservation.withStatus(Reservation.ReservationStatus.CHECKED_IN);
                    return setClientTimeInSession()
                            .then(reservationRepositoryPort.update(checkedInReservation));
                })
                .doOnNext(reservationSink::tryEmitNext)
        );
    }

    @Override
    public Mono<Reservation> checkOut(Long id) {
        return transactionalOperator.transactional(
            reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")))
                .flatMap(reservation -> {
                    if (!reservation.canCheckOut()) {
                        return Mono.error(new IllegalArgumentException("No se puede hacer check-out"));
                    }
                    Reservation checkedOutReservation = reservation.withStatus(Reservation.ReservationStatus.CHECKED_OUT);
                    return setClientTimeInSession()
                            .then(reservationRepositoryPort.update(checkedOutReservation));
                })
                .doOnNext(reservationSink::tryEmitNext)
        );
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

    @Override
    public Flux<Reservation> getReservationStream() {
        return reservationSink.asFlux();
    }

    @Override
    public Mono<OwnerDashboardSummary> getDashboardSummary(Long motelId, LocalDate today) {
        LocalDate startDate = today.minusDays(6);
        return Mono.zip(
                reservationRepositoryPort.findTodayByMotelId(motelId, today).collectList(),
                roomRepositoryPort.findByMotelId(motelId).collectList()
        ).map(tuple -> {
            var reservations = tuple.getT1();
            var rooms = tuple.getT2();

            var reservationsByStatus = reservations.stream()
                    .collect(Collectors.groupingBy(r -> r.status().name(), Collectors.counting()));

            var dailyRevenue = reservations.stream()
                    .filter(r -> r.status() != Reservation.ReservationStatus.CANCELLED)
                    .mapToDouble(Reservation::totalPrice)
                    .sum();

            var activeReservations = (long) reservations.size();
            var totalRooms = (long) rooms.size();
            var occupancyRate = totalRooms > 0 ? (double) activeReservations / totalRooms : 0.0;

            return new OwnerDashboardSummary(
                    reservationsByStatus,
                    dailyRevenue,
                    occupancyRate,
                    totalRooms,
                    activeReservations
            );
        });
    }

    @Override
    public Flux<RoomStatusBoardResponse> getRoomStatusBoard(Long motelId) {
        return roomRepositoryPort.findByMotelId(motelId)
                .flatMap(room -> reservationRepositoryPort.findActiveReservationsByRoomId(room.id())
                        .collectList()
                        .map(reservations -> {
                            RoomStatusBoardResponse.RoomStatus status = RoomStatusBoardResponse.RoomStatus.AVAILABLE;
                            RoomStatusBoardResponse.ReservationInfo currentRes = null;

                            if (!reservations.isEmpty()) {
                                // Buscamos la reserva más relevante (ej. la que está en CHECKED_IN o la próxima CONFIRMED)
                                var res = reservations.get(0); // Simplificación
                                status = switch (res.status()) {
                                    case CHECKED_IN -> RoomStatusBoardResponse.RoomStatus.OCCUPIED;
                                    case CONFIRMED -> RoomStatusBoardResponse.RoomStatus.PENDING_CHECKIN;
                                    default -> RoomStatusBoardResponse.RoomStatus.AVAILABLE;
                                };

                                currentRes = new RoomStatusBoardResponse.ReservationInfo(
                                        res.id(),
                                        "Invitado", // En el futuro podríamos cruzar con el nombre del usuario
                                        res.confirmationCode(),
                                        res.checkInDate(),
                                        res.checkOutDate(),
                                        res.status()
                                );
                            }

                            return new RoomStatusBoardResponse(
                                    room.id(),
                                    room.number(),
                                    room.roomType(),
                                    status,
                                    currentRes
                            );
                        }));
    }

    @Override
    public Mono<Reservation> getByConfirmationCode(String code) {
        return reservationRepositoryPort.findByConfirmationCode(code)
                .switchIfEmpty(Mono.error(new RuntimeException("Código de confirmación no válido: " + code)));
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
        if (reservation.checkInDate().isBefore(LocalDateTime.now(BOGOTA).minusMinutes(5))) {
            return Mono.error(new IllegalArgumentException(
                    "La fecha de check-in no puede ser en el pasado"));
        }
        if (reservation.totalPrice() == null || reservation.totalPrice() <= 0) {
            return Mono.error(new IllegalArgumentException("El precio total debe ser mayor que cero"));
        }
        return Mono.empty();
    }
}