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

    /**
     * Convierte de entidad de persistencia a modelo de dominio (sin imágenes)
     */
    public Room toDomain(RoomEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Room(
                entity.id(),
                entity.motelId(),
                entity.number(),
                entity.roomType(),
                entity.price(),
                entity.description(),
                entity.isAvailable(),
                List.of() // Lista vacía por defecto
        );
    }

    /**
     * Convierte de entidad de persistencia a modelo de dominio (con imágenes)
     */
    public Room toDomain(RoomEntity entity, List<String> imageUrls) {
        if (entity == null) {
            return null;
        }
        return new Room(
                entity.id(),
                entity.motelId(),
                entity.number(),
                entity.roomType(),
                entity.price(),
                entity.description(),
                entity.isAvailable(),
                imageUrls != null ? imageUrls : List.of()
        );
    }

    /**
     * Convierte de modelo de dominio a entidad de persistencia
     */
    public RoomEntity toEntity(Room room) {
        if (room == null) {
            return null;
        }
        return new RoomEntity(
                room.id(),
                room.motelId(),
                room.number(),
                room.roomType(),
                room.price(),
                room.description(),
                room.isAvailable()
        );
    }
}