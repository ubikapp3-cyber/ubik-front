package com.ubik.motelmanagement.domain.port.in;

import com.ubik.motelmanagement.domain.model.Reservation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Puerto de entrada (Input Port) para casos de uso de Reservation
 * Define las operaciones disponibles desde la capa de aplicación
 */
public interface ReservationUseCasePort {

    /**
     * Crea una nueva reserva
     * @param reservation Reserva a crear
     * @return Mono con la reserva creada
     */
    Mono<Reservation> createReservation(Reservation reservation);

    /**
     * Obtiene una reserva por su ID
     * @param id ID de la reserva
     * @return Mono con la reserva encontrada
     */
    Mono<Reservation> getReservationById(Long id);

    /**
     * Obtiene todas las reservas
     * @return Flux con todas las reservas
     */
    Flux<Reservation> getAllReservations();

    /**
     * Obtiene reservas por ID de habitación
     * @param roomId ID de la habitación
     * @return Flux con las reservas de la habitación
     */
    Flux<Reservation> getReservationsByRoomId(Long roomId);

    /**
     * Obtiene reservas por ID de usuario
     * @param userId ID del usuario
     * @return Flux con las reservas del usuario
     */
    Flux<Reservation> getReservationsByUserId(Long userId);

    /**
     * Obtiene reservas activas por ID de habitación
     * @param roomId ID de la habitación
     * @return Flux con las reservas activas
     */
    Flux<Reservation> getActiveReservationsByRoomId(Long roomId);

    /**
     * Obtiene reservas por estado
     * @param status Estado de la reserva
     * @return Flux con las reservas en ese estado
     */
    Flux<Reservation> getReservationsByStatus(Reservation.ReservationStatus status);

    /**
     * Verifica disponibilidad de una habitación en un rango de fechas
     * @param roomId ID de la habitación
     * @param checkIn Fecha de check-in
     * @param checkOut Fecha de check-out
     * @return Mono con true si está disponible, false en caso contrario
     */
    Mono<Boolean> isRoomAvailable(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut);

    /**
     * Actualiza una reserva existente
     * @param id ID de la reserva a actualizar
     * @param reservation Datos actualizados de la reserva
     * @return Mono con la reserva actualizada
     */
    Mono<Reservation> updateReservation(Long id, Reservation reservation);

    /**
     * Confirma una reserva
     * @param id ID de la reserva
     * @return Mono con la reserva confirmada
     */
    Mono<Reservation> confirmReservation(Long id);

    /**
     * Cancela una reserva
     * @param id ID de la reserva
     * @return Mono con la reserva cancelada
     */
    Mono<Reservation> cancelReservation(Long id);

    /**
     * Realiza check-in de una reserva
     * @param id ID de la reserva
     * @return Mono con la reserva actualizada
     */
    Mono<Reservation> checkIn(Long id);

    /**
     * Realiza check-out de una reserva
     * @param id ID de la reserva
     * @return Mono con la reserva actualizada
     */
    Mono<Reservation> checkOut(Long id);

    /**
     * Elimina una reserva (solo si está cancelada)
     * @param id ID de la reserva a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteReservation(Long id);
}