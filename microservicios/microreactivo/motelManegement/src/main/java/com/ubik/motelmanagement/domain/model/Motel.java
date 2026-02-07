package com.ubik.motelmanagement.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo de dominio para Motel con estado de aprobación e información legal
 * Representa la entidad de negocio independiente de la infraestructura
 */
public record Motel(
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
        ApprovalStatus approvalStatus,
        LocalDateTime approvalDate,
        Long approvedByUserId,
        String rejectionReason,
        String rues,                      // Registro Único Empresarial y Social
        String rnt,                       // Registro Nacional de Turismo
        DocumentType ownerDocumentType,   // Tipo de documento del propietario
        String ownerDocumentNumber,       // Número de documento del propietario
        String ownerFullName,             // Nombre completo del propietario
        String legalRepresentativeName,   // Representante legal (si aplica)
        String legalDocumentUrl           // URL del documento legal principal
) {
    /**
     * Estados de aprobación del motel
     */
    public enum ApprovalStatus {
        PENDING,        // Pendiente de revisión
        UNDER_REVIEW,   // En proceso de revisión
        APPROVED,       // Aprobado y activo
        REJECTED        // Rechazado
    }

    /**
     * Tipos de documento de identidad
     */
    public enum DocumentType {
        CC,         // Cédula de Ciudadanía
        NIT,        // Número de Identificación Tributaria
        CE,         // Cédula de Extranjería
        PASAPORTE   // Pasaporte
    }

    /**
     * Constructor para creación de nuevos moteles (sin ID, estado PENDING)
     */
    public static Motel createNew(
            String name,
            String address,
            String phoneNumber,
            String description,
            String city,
            Long propertyId,
            List<String> imageUrls,
            Double latitude,
            Double longitude,
            String rues,
            String rnt,
            DocumentType ownerDocumentType,
            String ownerDocumentNumber,
            String ownerFullName,
            String legalRepresentativeName,
            String legalDocumentUrl
    ) {
        return new Motel(
                null,
                name,
                address,
                phoneNumber,
                description,
                city,
                propertyId,
                LocalDateTime.now(),
                imageUrls,
                latitude,
                longitude,
                ApprovalStatus.PENDING,  // Nuevo motel inicia como PENDING
                null,
                null,
                null,
                rues,
                rnt,
                ownerDocumentType,
                ownerDocumentNumber,
                ownerFullName,
                legalRepresentativeName,
                legalDocumentUrl
        );
    }

    /**
     * Constructor para actualización (mantiene ID, fecha de creación y estado de aprobación)
     */
    public Motel withUpdatedInfo(
            String name,
            String address,
            String phoneNumber,
            String description,
            String city,
            List<String> imageUrls,
            Double latitude,
            Double longitude,
            String rues,
            String rnt,
            DocumentType ownerDocumentType,
            String ownerDocumentNumber,
            String ownerFullName,
            String legalRepresentativeName,
            String legalDocumentUrl
    ) {
        return new Motel(
                this.id,
                name,
                address,
                phoneNumber,
                description,
                city,
                this.propertyId,
                this.dateCreated,
                imageUrls,
                latitude,
                longitude,
                this.approvalStatus,  // Mantener estado actual
                this.approvalDate,
                this.approvedByUserId,
                this.rejectionReason,
                rues,
                rnt,
                ownerDocumentType,
                ownerDocumentNumber,
                ownerFullName,
                legalRepresentativeName,
                legalDocumentUrl
        );
    }

    /**
     * Aprobar el motel
     */
    public Motel approve(Long adminUserId) {
        return new Motel(
                this.id,
                this.name,
                this.address,
                this.phoneNumber,
                this.description,
                this.city,
                this.propertyId,
                this.dateCreated,
                this.imageUrls,
                this.latitude,
                this.longitude,
                ApprovalStatus.APPROVED,
                LocalDateTime.now(),
                adminUserId,
                null,  // Limpiar razón de rechazo
                this.rues,
                this.rnt,
                this.ownerDocumentType,
                this.ownerDocumentNumber,
                this.ownerFullName,
                this.legalRepresentativeName,
                this.legalDocumentUrl
        );
    }

    /**
     * Rechazar el motel
     */
    public Motel reject(Long adminUserId, String reason) {
        return new Motel(
                this.id,
                this.name,
                this.address,
                this.phoneNumber,
                this.description,
                this.city,
                this.propertyId,
                this.dateCreated,
                this.imageUrls,
                this.latitude,
                this.longitude,
                ApprovalStatus.REJECTED,
                LocalDateTime.now(),
                adminUserId,
                reason,
                this.rues,
                this.rnt,
                this.ownerDocumentType,
                this.ownerDocumentNumber,
                this.ownerFullName,
                this.legalRepresentativeName,
                this.legalDocumentUrl
        );
    }

    /**
     * Poner en revisión
     */
    public Motel putUnderReview() {
        return new Motel(
                this.id,
                this.name,
                this.address,
                this.phoneNumber,
                this.description,
                this.city,
                this.propertyId,
                this.dateCreated,
                this.imageUrls,
                this.latitude,
                this.longitude,
                ApprovalStatus.UNDER_REVIEW,
                this.approvalDate,
                this.approvedByUserId,
                this.rejectionReason,
                this.rues,
                this.rnt,
                this.ownerDocumentType,
                this.ownerDocumentNumber,
                this.ownerFullName,
                this.legalRepresentativeName,
                this.legalDocumentUrl
        );
    }

    /**
     * Verifica si el motel está aprobado
     */
    public boolean isApproved() {
        return this.approvalStatus == ApprovalStatus.APPROVED;
    }

    /**
     * Verifica si el motel puede ser editado
     * Solo se puede editar si está PENDING o REJECTED
     */
    public boolean canBeEdited() {
        return this.approvalStatus == ApprovalStatus.PENDING ||
                this.approvalStatus == ApprovalStatus.REJECTED;
    }

    /**
     * Verifica si la información legal está completa
     */
    public boolean hasCompleteLegalInfo() {
        return rues != null && !rues.isBlank() &&
                rnt != null && !rnt.isBlank() &&
                ownerDocumentType != null &&
                ownerDocumentNumber != null && !ownerDocumentNumber.isBlank() &&
                ownerFullName != null && !ownerFullName.isBlank();
    }
}