import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Interfaz para la información de un motel pendiente de aprobación
 */
export interface MotelApproval {
  id: number;
  name: string;
  address: string;
  phoneNumber: string;
  description: string;
  city: string;
  propertyId: number;
  dateCreated: string;
  imageUrls: string[];
  latitude: number;
  longitude: number;
  approvalStatus: 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED';
  approvalDate?: string;
  approvedByUserId?: number;
  rejectionReason?: string;
  rues?: string;
  rnt?: string;
  ownerDocumentType?: string;
  ownerDocumentNumber?: string;
  ownerFullName?: string;
  legalRepresentativeName?: string;
  legalDocumentUrl?: string;
}

/**
 * Interfaz para las estadísticas de aprobación
 */
export interface ApprovalStatistics {
  totalMotels: number;
  pendingApproval: number;
  underReview: number;
  approved: number;
  rejected: number;
  withIncompleteLegalInfo: number;
}

/**
 * Interfaz para la respuesta de operaciones de aprobación
 */
export interface ApprovalOperationResponse {
  id: number;
  name: string;
  previousStatus: string;
  newStatus: string;
  message: string;
  operationDate: string;
  operatedByUserId: number;
}

/**
 * Servicio para gestionar las operaciones de administración de moteles
 */
@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private readonly API_URL = `${environment.apiUrl}/admin/motels`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene los moteles pendientes de aprobación
   */
  getPendingMotels(): Observable<MotelApproval[]> {
    return this.http.get<MotelApproval[]>(`${this.API_URL}/pending`);
  }

  /**
   * Obtiene moteles filtrados por estado
   * @param status Estado del motel (PENDING, UNDER_REVIEW, APPROVED, REJECTED, ALL)
   */
  getMotelsByStatus(status: string): Observable<MotelApproval[]> {
    if (status === 'ALL') {
      return this.http.get<MotelApproval[]>(this.API_URL);
    }
    return this.http.get<MotelApproval[]>(`${this.API_URL}?status=${status}`);
  }

  /**
   * Obtiene un motel por su ID
   */
  getMotelById(id: number): Observable<MotelApproval> {
    return this.http.get<MotelApproval>(`${this.API_URL}/${id}`);
  }

  /**
   * Aprueba un motel
   */
  approveMotel(motelId: number): Observable<ApprovalOperationResponse> {
    return this.http.post<ApprovalOperationResponse>(
      `${this.API_URL}/${motelId}/approve`,
      {}
    );
  }

  /**
   * Rechaza un motel con una razón
   */
  rejectMotel(motelId: number, reason: string): Observable<ApprovalOperationResponse> {
    return this.http.post<ApprovalOperationResponse>(
      `${this.API_URL}/${motelId}/reject`,
      { reason }
    );
  }

  /**
   * Pone un motel en estado de revisión
   */
  putMotelUnderReview(motelId: number): Observable<ApprovalOperationResponse> {
    return this.http.patch<ApprovalOperationResponse>(
      `${this.API_URL}/${motelId}/review`,
      {}
    );
  }

  /**
   * Obtiene las estadísticas de aprobación
   */
  getApprovalStatistics(): Observable<ApprovalStatistics> {
    return this.http.get<ApprovalStatistics>(`${this.API_URL}/statistics`);
  }
}