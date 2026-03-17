package com.ubik.motelmanagement.domain.model;

import com.ubik.motelmanagement.domain.model.Reservation;
import java.time.LocalDateTime;

/**
 * DTO para el estado de una habitación en el tablero del propietario
 */
public record RoomStatusBoardResponse(
        Long roomId,
        String roomNumber,
        String roomType,
        RoomStatus status,
        ReservationInfo currentReservation
) {
    public enum RoomStatus {
        AVAILABLE,
        OCCUPIED,
        PENDING_CHECKIN,
        PENDING_CHECKOUT,
        CLEANING
    }

    public record ReservationInfo(
            Long reservationId,
            String guestName,
            String confirmationCode,
            LocalDateTime checkInDate,
            LocalDateTime checkOutDate,
            Reservation.ReservationStatus reservationStatus
    ) {}
}
