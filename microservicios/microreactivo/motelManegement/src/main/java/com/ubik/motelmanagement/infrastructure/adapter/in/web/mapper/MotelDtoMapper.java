package com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para convertir entre DTOs web y modelo de dominio Motel
 */
@Component
public class MotelDtoMapper {

    /**
     * Convierte CreateMotelRequest a Motel de dominio
     */
    public Motel toDomain(CreateMotelRequest request) {
        if (request == null) {
            return null;
        }
        return new Motel(
                null, // El ID se generará en la BD
                request.name(),
                request.address(),
                request.phoneNumber(),
                request.description(),
                request.city(),
                request.propertyId(),
                LocalDateTime.now(),
                request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>()
        );
    }

    /**
     * Convierte UpdateMotelRequest a Motel de dominio (sin ID ni fecha)
     */
    public Motel toDomain(UpdateMotelRequest request) {
        if (request == null) {
            return null;
        }
        return new Motel(
                null, // Se establecerá en el servicio
                request.name(),
                request.address(),
                request.phoneNumber(),
                request.description(),
                request.city(),
                null, // Se mantendrá el existente
                null,  // Se mantendrá la existente
                request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>()
        );
    }

    /**
     * Convierte Motel de dominio a MotelResponse
     */
    public MotelResponse toResponse(Motel motel) {
        if (motel == null) {
            return null;
        }
        return new MotelResponse(
                motel.id(),
                motel.name(),
                motel.address(),
                motel.phoneNumber(),
                motel.description(),
                motel.city(),
                motel.propertyId(),
                motel.dateCreated(),
                motel.imageUrls()
        );
    }
}