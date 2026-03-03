import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Room } from '../models/room.model';

@Injectable({
  providedIn: 'root'
})
export class RoomService {

  private readonly API_URL = `${environment.apiUrl}/rooms`;

  constructor(private http: HttpClient) {}

  getRooms(): Observable<Room[]> {
    return this.http.get<Room[]>(this.API_URL);
  }

  getRoomById(id: number): Observable<Room> {
    return this.http.get<Room>(`${this.API_URL}/${id}`);
  }

  getRoomsByMotel(motelId: number): Observable<Room[]> {
    return this.http.get<Room[]>(`${this.API_URL}/motel/${motelId}`);
  }
}