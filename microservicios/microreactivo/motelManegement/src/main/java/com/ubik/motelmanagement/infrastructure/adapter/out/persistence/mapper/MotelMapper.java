package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.mapper;

import com.ubik.motelmanagement.domain.model.ApprovalStatus;
import com.ubik.motelmanagement.domain.model.DocumentType;
import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.model.ApprovalStatus;
import com.ubik.motelmanagement.domain.model.DocumentType;
import com.ubik.motelmanagement.domain.model.MotelImage;
import com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity.MotelEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper para convertir entre el modelo de dominio Motel y la entidad de persistencia MotelEntity
 * Incluye mapeo de campos de aprobación e información legal
 *
 */
@Component
public class MotelMapper {

    /**
     * Convierte de entidad de persistencia a modelo de dominio (sin imágenes)
     */
    public Motel toDomain(MotelEntity entity) {
        if (entity == null) return null;

        return new Motel(
                entity.id(),
                entity.name(),
                entity.address(),
                entity.phoneNumber(),
                entity.description(),
                entity.city(),
                entity.propertyId(),
                entity.dateCreated(),
                List.of(), // ahora lista vacía de MotelImage
                entity.latitude(),
                entity.longitude(),
                entity.approvalStatus() != null
                        ? ApprovalStatus.valueOf(entity.approvalStatus().toUpperCase())
                        : ApprovalStatus.PENDING,
                entity.approvalDate(),
                entity.approvedByUserId(),
                entity.rejectionReason(),
                entity.rues(),
                entity.rnt(),
                entity.ownerDocumentType() != null
                        ? DocumentType.valueOf(entity.ownerDocumentType().toUpperCase())
                        : null,
                entity.ownerDocumentNumber(),
                entity.ownerFullName(),
                entity.legalRepresentativeName(),
                entity.legalDocumentUrl()
        );
    }

    /**
     * Convierte de entidad de persistencia a modelo de dominio (con imágenes)
     */
    public Motel toDomain(MotelEntity entity, List<MotelImage> images) {
        if (entity == null) return null;

        return new Motel(
                entity.id(),
                entity.name(),
                entity.address(),
                entity.phoneNumber(),
                entity.description(),
                entity.city(),
                entity.propertyId(),
                entity.dateCreated(),
                images != null ? images : List.of(),
                entity.latitude(),
                entity.longitude(),
                entity.approvalStatus() != null
                        ? ApprovalStatus.valueOf(entity.approvalStatus().toUpperCase())
                        : ApprovalStatus.PENDING,
                entity.approvalDate(),
                entity.approvedByUserId(),
                entity.rejectionReason(),
                entity.rues(),
                entity.rnt(),
                entity.ownerDocumentType() != null
                        ? DocumentType.valueOf(entity.ownerDocumentType().toUpperCase())
                        : null,
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
        if (motel == null) return null;

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
                motel.approvalStatus() != null
                        ? motel.approvalStatus().name()
                        : ApprovalStatus.PENDING.name(),
                motel.approvalDate(),
                motel.approvedByUserId(),
                motel.rejectionReason(),
                motel.rues(),
                motel.rnt(),
                motel.ownerDocumentType() != null
                        ? motel.ownerDocumentType().name()
                        : null,
                motel.ownerDocumentNumber(),
                motel.ownerFullName(),
                motel.legalRepresentativeName(),
                motel.legalDocumentUrl()
        );
    }
}