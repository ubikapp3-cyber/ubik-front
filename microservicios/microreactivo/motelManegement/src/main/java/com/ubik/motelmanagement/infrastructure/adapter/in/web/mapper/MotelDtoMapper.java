package com.ubik.motelmanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.motelmanagement.domain.model.ApprovalStatus;
import com.ubik.motelmanagement.domain.model.DocumentType;
import com.ubik.motelmanagement.domain.model.ImageRole;
import com.ubik.motelmanagement.domain.model.Motel;
import com.ubik.motelmanagement.domain.model.ApprovalStatus;
import com.ubik.motelmanagement.domain.model.DocumentType;
import com.ubik.motelmanagement.domain.model.DocumentType;
import com.ubik.motelmanagement.domain.model.MotelImage;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.motelmanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class MotelDtoMapper {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    public Motel toDomain(CreateMotelRequest request) {
        if (request == null) return null;

        DocumentType docType = parseDocumentTypeOrThrow(request.ownerDocumentType());

        List<MotelImage> images = urlsToGalleryImages(request.imageUrls());

        return new Motel(
                null,
                request.name(),
                request.address(),
                request.phoneNumber(),
                request.description(),
                request.city(),
                request.propertyId(),
                LocalDateTime.now(BOGOTA),
                images,
                request.latitude(),
                request.longitude(),
                ApprovalStatus.PENDING,
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

    public Motel toDomain(UpdateMotelRequest request) {
        if (request == null) return null;

        // En update el docType puede venir null (según tu DTO actual)
        DocumentType docType = parseDocumentTypeOrNull(request.ownerDocumentType());

        List<MotelImage> images = urlsToGalleryImages(request.imageUrls());

        // Mantengo tu mismo patrón actual: id/propertyId/dateCreated/approval* en null
        // porque el dominio MotelService reconstruye el objeto final usando existingMotel.*.
        return new Motel(
                null,
                request.name(),
                request.address(),
                request.phoneNumber(),
                request.description(),
                request.city(),
                null,
                null,
                images,
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
        if (motel == null) return null;

        return new MotelResponse(
                motel.id(),
                motel.name(),
                motel.address(),
                motel.phoneNumber(),
                motel.description(),
                motel.city(),
                motel.propertyId(),
                motel.dateCreated(),
                motel.imageUrls(), // ahora es List<MotelImage>
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

    // =========================
    // Helpers
    // =========================

    private DocumentType parseDocumentTypeOrThrow(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Tipo de documento inválido: " + raw);
        }
        try {
            return DocumentType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de documento inválido: " + raw);
        }
    }

    private DocumentType parseDocumentTypeOrNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return DocumentType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de documento inválido: " + raw);
        }
    }

    /**
     * Convierte List<String> (urls) => List<MotelImage> como GALLERY con sortOrder incremental.
     * (PROFILE/COVER se setean por endpoints específicos, no desde estos DTOs)
     */
    private List<MotelImage> urlsToGalleryImages(List<String> urls) {
        List<MotelImage> images = new ArrayList<>();
        if (urls == null || urls.isEmpty()) return images;

        int order = 1;
        for (String url : urls) {
            if (url == null || url.isBlank()) continue;
            images.add(new MotelImage(null, url.trim(), ImageRole.GALLERY, order++));
        }
        return images;
    }
}