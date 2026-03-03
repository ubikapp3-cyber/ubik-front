import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CreateReservationRequest {
  roomId: number;
  userId: number;
  checkInDate: string;
  checkOutDate: string;
  totalPrice: number;
  specialRequests?: string;
}

export interface ReservationResponse {
  id: number;
  roomId: number;
  userId: number;
  checkInDate: string;
  checkOutDate: string;
  status: string;
  totalPrice: number;
  specialRequests: string;
  confirmationCode: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReservationService {

  private readonly API_URL = `${environment.apiUrl}/reservations`;

  constructor(private http: HttpClient) {}

  createReservation(request: CreateReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(this.API_URL, request);
  }
}
