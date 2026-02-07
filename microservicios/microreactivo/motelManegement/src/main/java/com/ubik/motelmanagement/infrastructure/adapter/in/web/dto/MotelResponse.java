package com.ubik.motelmanagement.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para Motel con estado de aprobación e información legal
 */
public record MotelResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String description,
        String city,
        Long propertyId,
        LocalDateTime dateCreated,
        List<String> imageUrls,
        Double latitude,
        Double longitude,
        String approvalStatus,
        LocalDateTime approvalDate,
        Long approvedByUserId,
        String rejectionReason,
        String rues,
        String rnt,
        String ownerDocumentType,
        String ownerDocumentNumber,
        String ownerFullName,
        String legalRepresentativeName,
        String legalDocumentUrl,
        Boolean hasCompleteLegalInfo
) {
}