package com.ubik.motelmanagement.domain.model;

/**
 * Estados de aprobación del motel
 */
public enum ApprovalStatus {
    PENDING,        // Pendiente de revisión
    UNDER_REVIEW,   // En proceso de revisión
    APPROVED,       // Aprobado y activo
    REJECTED        // Rechazado
}
