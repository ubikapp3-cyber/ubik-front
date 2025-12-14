package com.ubik.motelmanagement.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para Reservation
 * Representa la entidad de negocio de reservas independiente de la infraestructura
 */
public record Reservation(
        Long id,
        Long roomId,
        Long userId,
        LocalDateTime checkInDate,
        LocalDateTime checkOutDate,
        ReservationStatus status,
        Double totalPrice,
        String specialRequests,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Estados posibles de una reserva
     */
    public enum ReservationStatus {
        PENDING,      // Pendiente de confirmación
        CONFIRMED,    // Confirmada
        CHECKED_IN,   // Cliente ya hizo check-in
        CHECKED_OUT,  // Cliente ya hizo check-out
        CANCELLED     // Cancelada
    }

    /**
     * Constructor para creación de nuevas reservas
     */
    public static Reservation createNew(
            Long roomId,
            Long userId,
            LocalDateTime checkInDate,
            LocalDateTime checkOutDate,
            Double totalPrice,
            String specialRequests
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new Reservation(
                null,
                roomId,
                userId,
                checkInDate,
                checkOutDate,
                ReservationStatus.PENDING,
                totalPrice,
                specialRequests,
                now,
                now
        );
    }

    /**
     * Constructor para actualización del estado
     */
    public Reservation withStatus(ReservationStatus newStatus) {
        return new Reservation(
                this.id,
                this.roomId,
                this.userId,
                this.checkInDate,
                this.checkOutDate,
                newStatus,
                this.totalPrice,
                this.specialRequests,
                this.createdAt,
                LocalDateTime.now()
        );
    }

    /**
     * Constructor para actualización completa
     */
    public Reservation withUpdatedInfo(
            LocalDateTime checkInDate,
            LocalDateTime checkOutDate,
            Double totalPrice,
            String specialRequests
    ) {
        return new Reservation(
                this.id,
                this.roomId,
                this.userId,
                checkInDate,
                checkOutDate,
                this.status,
                totalPrice,
                specialRequests,
                this.createdAt,
                LocalDateTime.now()
        );
    }

    /**
     * Verifica si la reserva está activa (no cancelada ni completada)
     */
    public boolean isActive() {
        return status != ReservationStatus.CANCELLED &&
                status != ReservationStatus.CHECKED_OUT;
    }

    /**
     * Verifica si la reserva puede ser cancelada
     */
    public boolean canBeCancelled() {
        return status == ReservationStatus.PENDING ||
                status == ReservationStatus.CONFIRMED;
    }

    /**
     * Verifica si la reserva puede ser confirmada
     */
    public boolean canBeConfirmed() {
        return status == ReservationStatus.PENDING;
    }

    /**
     * Verifica si se puede hacer check-in
     */
    public boolean canCheckIn() {
        return status == ReservationStatus.CONFIRMED;
    }

    /**
     * Verifica si se puede hacer check-out
     */
    public boolean canCheckOut() {
        return status == ReservationStatus.CHECKED_IN;
    }
}