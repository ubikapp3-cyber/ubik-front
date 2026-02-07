package com.ubik.motelmanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para Motel con estado de aprobación e información legal
 * Representa la tabla en la base de datos
 */
@Table("motel")
public record MotelEntity(
        @Id Long id,
        String name,
        String address,
        @Column("phone_number") String phoneNumber,
        String description,
        String city,
        @Column("property_id") Long propertyId,
        @Column("date_created") LocalDateTime dateCreated,
        Double latitude,
        Double longitude,

        @Column("approval_status") String approvalStatus,
        @Column("approval_date") LocalDateTime approvalDate,
        @Column("approved_by_user_id") Long approvedByUserId,
        @Column("rejection_reason") String rejectionReason,
        String rues,
        String rnt,
        @Column("owner_document_type") String ownerDocumentType,
        @Column("owner_document_number") String ownerDocumentNumber,
        @Column("owner_full_name") String ownerFullName,
        @Column("legal_representative_name") String legalRepresentativeName,
        @Column("legal_document_url") String legalDocumentUrl
) {
}