package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapper para convertir entre el modelo de dominio Motel y la entidad de persistencia MotelEntity
 */
@Component
public class MotelMapper {

    /**
     * Convierte de entidad de persistencia a modelo de dominio (sin imágenes)
     */
    public Motel toDomain(MotelEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Motel(
                entity.id(),
                entity.name(),
                entity.address(),
                entity.phoneNumber(),
                entity.description(),
                entity.city(),
                entity.propertyId(),
                entity.dateCreated(),
                List.of() // Lista vacía por defecto
        );
    }

    /**
     * Convierte de entidad de persistencia a modelo de dominio (con imágenes)
     */
    public Motel toDomain(MotelEntity entity, List<String> imageUrls) {
        if (entity == null) {
            return null;
        }
        return new Motel(
                entity.id(),
                entity.name(),
                entity.address(),
                entity.phoneNumber(),
                entity.description(),
                entity.city(),
                entity.propertyId(),
                entity.dateCreated(),
                imageUrls != null ? imageUrls : List.of()
        );
    }

    /**
     * Convierte de modelo de dominio a entidad de persistencia
     */
    public MotelEntity toEntity(Motel motel) {
        if (motel == null) {
            return null;
        }
        return new MotelEntity(
                motel.id(),
                motel.name(),
                motel.address(),
                motel.phoneNumber(),
                motel.description(),
                motel.city(),
                motel.propertyId(),
                motel.dateCreated()
        );
    }
}