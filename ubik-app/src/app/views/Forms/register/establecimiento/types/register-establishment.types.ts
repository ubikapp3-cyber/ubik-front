// src/app/models/motel.model.ts (similar a PDF)
export type ApprovalStatus = 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
export type DocumentType = 'CC' | 'NIT' | 'CE' | 'PASAPORTE';

export interface Motel {
  id: number;
  name: string;
  address: string;
  phoneNumber?: string;
  description?: string;
  city: string;
  propertyId: number;
  dateCreated: string;
  imageUrls?: string[];
  latitude?: number;
  longitude?: number;
  approvalStatus: ApprovalStatus;
  approvalDate?: string;
  approvedByUserId?: number;
  rejectionReason?: string;

  // En tu caso: aquí guardarás URLs de Cloudinary
  rues?: string;
  rnt?: string;

  ownerDocumentType?: DocumentType;
  ownerDocumentNumber?: string;
  ownerFullName?: string;
  legalRepresentativeName?: string;
  legalDocumentUrl?: string;
  hasCompleteLegalInfo: boolean;
}

export interface CreateMotelRequest {
  name: string;
  address: string;
  phoneNumber?: string;
  description?: string;
  city: string;
  propertyId: number;
  imageUrls?: string[];
  latitude?: number;
  longitude?: number;

  // URLs (Cloudinary) en tu implementación
  rues: string;
  rnt: string;

  ownerDocumentType: DocumentType;
  ownerDocumentNumber: string;
  ownerFullName: string;
  legalRepresentativeName?: string;
  legalDocumentUrl?: string;
}
