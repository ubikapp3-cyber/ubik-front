package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper;

import com.ubik.motelmanagement.domain.model.Room;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.RoomEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper para convertir entre el modelo de dominio Room y la entidad de persistencia RoomEntity
 */
@Component
public class RoomMapper {

    /** Entidad → Dominio sin imágenes ni serviceIds */
    public Room toDomain(RoomEntity entity) {
        if (entity == null) return null;
        return new Room(
                entity.id(), entity.motelId(), entity.number(), entity.roomType(),
                entity.price(), entity.description(), entity.isAvailable(),
                List.of(),
                entity.latitude(), entity.longitude(),
                null, null, null, null,
                null
        );
    }

    /** Entidad → Dominio con imágenes y serviceIds */
    public Room toDomain(RoomEntity entity, List<String> imageUrls, List<Long> serviceIds) {
        if (entity == null) return null;
        return new Room(
                entity.id(), entity.motelId(), entity.number(), entity.roomType(),
                entity.price(), entity.description(), entity.isAvailable(),
                imageUrls != null ? imageUrls : List.of(),
                entity.latitude(), entity.longitude(),
                null, null, null, null,
                serviceIds != null ? serviceIds : List.of()
        );
    }

    /** Dominio → Entidad (solo campos de la tabla room) */
    public RoomEntity toEntity(Room room) {
        if (room == null) return null;
        return new RoomEntity(
                room.id(), room.motelId(), room.number(), room.roomType(),
                room.price(), room.description(), room.isAvailable(),
                null, null
        );
    }
}