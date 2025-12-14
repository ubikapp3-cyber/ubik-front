package com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.motelmanagement.domain.model.Room;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateRoomRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.RoomResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateRoomRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para convertir entre DTOs web y modelo de dominio Room
 */
@Component
public class RoomDtoMapper {

    /**
     * Convierte CreateRoomRequest a Room de dominio
     */
    public Room toDomain(CreateRoomRequest request) {
        if (request == null) {
            return null;
        }
        return new Room(
                null, // El ID se generará en la BD
                request.motelId(),
                request.number(),
                request.roomType(),
                request.price(),
                request.description(),
                true, // Por defecto disponible
                request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>()
        );
    }

    /**
     * Convierte UpdateRoomRequest a Room de dominio (sin ID ni motelId)
     */
    public Room toDomain(UpdateRoomRequest request) {
        if (request == null) {
            return null;
        }
        return new Room(
                null, // Se establecerá en el servicio
                null, // Se mantendrá el existente
                request.number(),
                request.roomType(),
                request.price(),
                request.description(),
                request.isAvailable(),
                request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>()
        );
    }

    /**
     * Convierte Room de dominio a RoomResponse
     */
    public RoomResponse toResponse(Room room) {
        if (room == null) {
            return null;
        }
        return new RoomResponse(
                room.id(),
                room.motelId(),
                room.number(),
                room.roomType(),
                room.price(),
                room.description(),
                room.isAvailable(),
                room.imageUrls()
        );
    }
}