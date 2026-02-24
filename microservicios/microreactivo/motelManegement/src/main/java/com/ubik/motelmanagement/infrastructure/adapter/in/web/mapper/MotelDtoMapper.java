package com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class MotelDtoMapper {

    public Motel toDomain(CreateMotelRequest request) {
        if (request == null) {
            return null;
        }

        Motel.DocumentType docType = parseDocumentType(request.ownerDocumentType());

        return new Motel(
                null,
                request.name(),
                request.address(),
                request.phoneNumber(),
                request.description(),
                request.city(),
                request.propertyId(),
                LocalDateTime.now(),
                request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>(),
                request.latitude(),
                request.longitude(),
                Motel.ApprovalStatus.PENDING,
                null,
                null,
                null,
                request.rues(),
                request.rnt(),
                docType,
                request.ownerDocumentNumber(),
                request.ownerFullName(),
                request.legalRepresentativeName(),
                request.legalDocumentUrl()
        );
    }

    /**
     * Convierte CreateMotelRequest a dominio inyectando el propertyId del usuario autenticado.
     * Usar este método en todos los controllers para evitar duplicar la construcción del record.
     *
     * @param request  DTO de creación
     * @param ownerId  userId resuelto desde la BD (nunca el claim del JWT directamente)
     */
    public Motel toDomainWithOwner(CreateMotelRequest request, Long ownerId) {
        Motel base = toDomain(request);
        if (base == null) return null;

        return new Motel(
                base.id(),
                base.name(),
                base.address(),
                base.phoneNumber(),
                base.description(),
                base.city(),
                ownerId,                    // único campo que sobreescribimos
                base.dateCreated(),
                base.imageUrls(),
                base.latitude(),
                base.longitude(),
                base.approvalStatus(),
                base.approvalDate(),
                base.approvedByUserId(),
                base.rejectionReason(),
                base.rues(),
                base.rnt(),
                base.ownerDocumentType(),
                base.ownerDocumentNumber(),
                base.ownerFullName(),
                base.legalRepresentativeName(),
                base.legalDocumentUrl()
        );
    }

    public Motel toDomain(UpdateMotelRequest request) {
        if (request == null) {
            return null;
        }

        Motel.DocumentType docType = parseDocumentType(request.ownerDocumentType());

        return new Motel(
                null,
                request.name(),
                request.address(),
                request.phoneNumber(),
                request.description(),
                request.city(),
                null,
                null,
                request.imageUrls() != null ? new ArrayList<>(request.imageUrls()) : new ArrayList<>(),
                request.latitude(),
                request.longitude(),
                null,
                null,
                null,
                null,
                request.rues(),
                request.rnt(),
                docType,
                request.ownerDocumentNumber(),
                request.ownerFullName(),
                request.legalRepresentativeName(),
                request.legalDocumentUrl()
        );
    }

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
                motel.imageUrls(),
                motel.latitude(),
                motel.longitude(),
                motel.approvalStatus() != null ? motel.approvalStatus().name() : null,
                motel.approvalDate(),
                motel.approvedByUserId(),
                motel.rejectionReason(),
                motel.rues(),
                motel.rnt(),
                motel.ownerDocumentType() != null ? motel.ownerDocumentType().name() : null,
                motel.ownerDocumentNumber(),
                motel.ownerFullName(),
                motel.legalRepresentativeName(),
                motel.legalDocumentUrl(),
                motel.hasCompleteLegalInfo()
        );
    }

    // ─── Helper privado ──────────────────────────────────────────────────────────

    /**
     * Parsea el tipo de documento de forma segura.
     * Lanza IllegalArgumentException con mensaje claro si el valor no es válido.
     */
    private Motel.DocumentType parseDocumentType(String raw) {
        if (raw == null) return null;
        try {
            return Motel.DocumentType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Tipo de documento inválido: '" + raw + "'. " +
                            "Valores permitidos: CC, NIT, CE, PASAPORTE");
        }
    }
}