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
        
        Motel.DocumentType docType = null;
        if (request.ownerDocumentType() != null) {
            try {
                docType = Motel.DocumentType.valueOf(request.ownerDocumentType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de documento inválido: " + request.ownerDocumentType());
            }
        }
        
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

    public Motel toDomain(UpdateMotelRequest request) {
        if (request == null) {
            return null;
        }
        
        Motel.DocumentType docType = null;
        if (request.ownerDocumentType() != null) {
            try {
                docType = Motel.DocumentType.valueOf(request.ownerDocumentType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de documento inválido: " + request.ownerDocumentType());
            }
        }
        
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
}