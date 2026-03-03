import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CreatePaymentRequest {
  reservationId: number;
  motelId: number;
  amount: number;
  currency?: string;
}

export interface PaymentResponse {
  id: number;
  reservationId: number;
  userId: number;
  amount: number;
  currency: string;
  status: string;
  mercadoPagoPaymentId: string;
  mercadoPagoPreferenceId: string;
  initPoint: string;
  failureReason: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {

  private readonly API_URL = `${environment.apiUrl}/payments`;

  constructor(private http: HttpClient) {}

  createPayment(request: CreatePaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(this.API_URL, request);
  }
}
