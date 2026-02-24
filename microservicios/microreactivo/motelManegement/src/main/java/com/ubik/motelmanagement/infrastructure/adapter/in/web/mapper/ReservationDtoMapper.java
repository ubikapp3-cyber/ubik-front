package com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.motelmanagement.domain.model.Reservation;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateReservationRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.ReservationResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateReservationRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper para convertir entre DTOs web y modelo de dominio Reservation
 * ✅ CORREGIDO: Mantiene fiabilidad del confirmationCode
 */
@Component
public class ReservationDtoMapper {

    /**
     * ✅ Convierte CreateReservationRequest a Reservation de dominio
     * confirmationCode = null porque se genera en el servicio
     */
    public Reservation toDomain(CreateReservationRequest request) {
        if (request == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        return new Reservation(
                null, // El ID se generará en la BD
                request.roomId(),
                request.userId(),
                request.checkInDate(),
                request.checkOutDate(),
                Reservation.ReservationStatus.PENDING,
                request.totalPrice(),
                request.specialRequests(),
                null,  // confirmationCode = null (se genera en el servicio)
                now,
                now
        );
    }

    /**
     * Convierte UpdateReservationRequest a Reservation de dominio
     * confirmationCode = null porque NO se puede actualizar
     * (el servicio mantendrá el código original)
     */
    public Reservation toDomain(UpdateReservationRequest request) {
        if (request == null) {
            return null;
        }
        return new Reservation(
                null, // Se establecerá en el servicio
                null, // Se mantendrá el existente
                null, // Se mantendrá el existente
                request.checkInDate(),
                request.checkOutDate(),
                null, // Se mantendrá el existente
                request.totalPrice(),
                request.specialRequests(),
                null, // confirmationCode = null (se mantiene el original en el servicio)
                null, // Se mantendrá la existente
                LocalDateTime.now()
        );
    }

    /**
     * Convierte Reservation de dominio a ReservationResponse
     * INCLUYE confirmationCode en la respuesta
     */
    public ReservationResponse toResponse(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        return new ReservationResponse(
                reservation.id(),
                reservation.roomId(),
                reservation.userId(),
                reservation.checkInDate(),
                reservation.checkOutDate(),
                reservation.status().name(),
                reservation.totalPrice(),
                reservation.specialRequests(),
                reservation.confirmationCode(),
                reservation.createdAt(),
                reservation.updatedAt()
        );
    }
}