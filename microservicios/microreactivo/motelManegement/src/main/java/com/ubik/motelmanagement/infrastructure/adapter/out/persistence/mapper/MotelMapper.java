package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mapper para convertir entre el modelo de dominio Motel y la entidad de persistencia MotelEntity
 * Incluye mapeo de campos de aprobación e información legal
 *
 * ✅ FIXED: Maneja conversión de enums case-insensitive para evitar errores con datos en BD
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
                List.of(), // Lista vacía por defecto
                entity.latitude(),
                entity.longitude(),
                // ✅ FIX: Convertir a uppercase antes de valueOf
                entity.approvalStatus() != null ?
                        Motel.ApprovalStatus.valueOf(entity.approvalStatus().toUpperCase()) :
                        Motel.ApprovalStatus.PENDING,
                entity.approvalDate(),
                entity.approvedByUserId(),
                entity.rejectionReason(),
                entity.rues(),
                entity.rnt(),
                // ✅ FIX: Convertir a uppercase antes de valueOf
                entity.ownerDocumentType() != null ?
                        Motel.DocumentType.valueOf(entity.ownerDocumentType().toUpperCase()) :
                        null,
                entity.ownerDocumentNumber(),
                entity.ownerFullName(),
                entity.legalRepresentativeName(),
                entity.legalDocumentUrl()
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
                imageUrls != null ? imageUrls : List.of(),
                entity.latitude(),
                entity.longitude(),
                // Campos de aprobación
                // ✅ FIX: Convertir a uppercase antes de valueOf
                entity.approvalStatus() != null ?
                        Motel.ApprovalStatus.valueOf(entity.approvalStatus().toUpperCase()) :
                        Motel.ApprovalStatus.PENDING,
                entity.approvalDate(),
                entity.approvedByUserId(),
                entity.rejectionReason(),
                // Campos de información legal
                entity.rues(),
                entity.rnt(),
                // ✅ FIX: Convertir a uppercase antes de valueOf
                entity.ownerDocumentType() != null ?
                        Motel.DocumentType.valueOf(entity.ownerDocumentType().toUpperCase()) :
                        null,
                entity.ownerDocumentNumber(),
                entity.ownerFullName(),
                entity.legalRepresentativeName(),
                entity.legalDocumentUrl()
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
                motel.dateCreated(),
                motel.latitude(),
                motel.longitude(),
                motel.approvalStatus() != null ?
                        motel.approvalStatus().name() :
                        Motel.ApprovalStatus.PENDING.name(),
                motel.approvalDate(),
                motel.approvedByUserId(),
                motel.rejectionReason(),
                motel.rues(),
                motel.rnt(),
                motel.ownerDocumentType() != null ?
                        motel.ownerDocumentType().name() :
                        null,
                motel.ownerDocumentNumber(),
                motel.ownerFullName(),
                motel.legalRepresentativeName(),
                motel.legalDocumentUrl()
        );
    }
}