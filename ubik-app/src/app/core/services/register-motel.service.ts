// src/app/services/motel.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Motel, CreateMotelRequest } from '../../views/Forms/register/establecimiento/types/register-establishment.types';

@Injectable({ providedIn: 'root' })
export class MotelService {
  private apiUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) {}


  createMotel(motel: CreateMotelRequest): Observable<Motel> {
    return this.http.post<Motel>(`${this.apiUrl}/motels`, motel);
  }
}
