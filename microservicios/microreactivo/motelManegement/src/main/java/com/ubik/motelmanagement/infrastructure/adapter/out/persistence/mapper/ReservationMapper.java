package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper;

import com.ubik.motelmanagement.domain.model.Reservation;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre entidad de persistencia y modelo de dominio para Reservation
 */
@Component
public class ReservationMapper {

    /**
     * Convierte de entidad de persistencia a modelo de dominio
     */
    public Reservation toDomain(ReservationEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Reservation(
                entity.id(),
                entity.roomId(),
                entity.userId(),
                entity.checkInDate(),
                entity.checkOutDate(),
                Reservation.ReservationStatus.valueOf(entity.status()),
                entity.totalPrice(),
                entity.specialRequests(),
                entity.confirmationCode(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

    /**
     * Convierte de modelo de dominio a entidad de persistencia
     */
    public ReservationEntity toEntity(Reservation reservation) {
        if (reservation == null) {
            return null;
        }

        return new ReservationEntity(
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
