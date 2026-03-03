import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, EMPTY, tap, shareReplay } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { environment } from '../../../environments/environment';
import { Users } from '../models/users.model';

export type UpdateUserProfileDto = Partial<Pick<Users,
  'username' | 'email' | 'phoneNumber' | 'anonymous' | 'birthDate' | 'longitude' | 'latitude'
>>;

@Injectable({ providedIn: 'root' })
export class UsersService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private baseUrl = `${environment.apiUrl}/user`;

  private profileSubject = new BehaviorSubject<Users | null>(null);
  profile$ = this.profileSubject.asObservable();

  loadProfile(): Observable<Users> {
    if (!isPlatformBrowser(this.platformId)) return EMPTY;

    return this.http.get<Users>(this.baseUrl).pipe(
      tap(profile => this.profileSubject.next(profile)),
      shareReplay(1)
    );
  }

  /** Útil si ya tienes datos y no quieres refetch */
  setProfile(profile: Users) {
    this.profileSubject.next(profile);
  }

  updateProfile(dto: UpdateUserProfileDto): Observable<Users> {
    if (!isPlatformBrowser(this.platformId)) return EMPTY;

    return this.http.put<Users>(this.baseUrl, dto).pipe(
      tap(updated => this.profileSubject.next(updated))
    );
  }

  deleteProfile(): Observable<void> {
    if (!isPlatformBrowser(this.platformId)) return EMPTY;  

    return this.http.delete<void>(this.baseUrl);
  }

  clearProfile() {
    this.profileSubject.next(null);
  }

  /** Por si quieres forzar refresh manual */
  refreshProfile(): Observable<Users> {
    return this.loadProfile();
  }
}