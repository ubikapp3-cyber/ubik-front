import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, delay, tap } from 'rxjs/operators';
import {
  LoginFormData,
  AuthResult,
  ValidationError,
  OAuthProvider,
} from '../../views/Forms/login/types/login.types';
import { environment } from '../../../environments/environment';
import { AuthService } from '../middleware/auth.service';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private readonly apiUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
  ) {}

  /* ======================= */

  validateForm(data: Partial<LoginFormData>): ValidationError[] {
    const errors: ValidationError[] = [];

    if (!data.username?.trim()) {
      errors.push({ field: 'username', message: 'El usuario es obligatorio' });
    }

    if (!data.password?.trim()) {
      errors.push({ field: 'password', message: 'La contraseña es obligatoria' });
    }

    return errors;
  }

  /* ======================= */

  login(data: LoginFormData, rememberMe: boolean): Observable<string> {
    return this.http.post(`${this.apiUrl}/auth/login`, data, { responseType: 'text' }).pipe(
      tap((rawToken: string) => {
        const token = rawToken.replace(/"/g, '').trim();

        console.log('TOKEN LIMPIO →', token);

        this.auth.setToken(token, rememberMe);
      }),
    );
  }

  /* ======================= */
  /* PERFIL DEL USUARIO */
  /* ======================= */

  getProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/user`).pipe(
      tap((user) => this.auth.setUser(user)),
      catchError((error) => {
        return throwError(() => error);
      }),
    );
  }

  /* ======================= */

  getAuthToken(): string | null {
    return this.auth.token();
  }

  clearAuthToken(): void {
    this.auth.logout();
  }

  isAuthenticated(): boolean {
    return this.auth.isLogged();
  }

  /* ======================= */

  loginWithOAuth(provider: OAuthProvider): Observable<AuthResult> {
    return of({
      success: true,
      message: 'Login exitoso (mock)',
      token: `mock-oauth-token-${provider}-${Date.now()}`,
      userId: '123',
      redirectUrl: '/home',
    }).pipe(delay(500));
  }
}
