/**
 * Authentication API Service
 * Follows Single Responsibility Principle (SRP) - handles auth API communication only
 * Follows Dependency Inversion Principle (DIP) - depends on HttpClientService abstraction
 * 
 * Provides methods for all authentication-related API calls
 */

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpClientService } from '../../../../core/services/http-client.service';
import {
  LoginRequestDto,
  LoginResponseDto,
  RegisterClientRequestDto,
  RegisterEstablishmentRequestDto,
  RegisterResponseDto,
  PasswordResetRequestDto,
  PasswordResetDto,
} from '../../../../core/models/api.models';

@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly AUTH_BASE_PATH = '/auth';

  constructor(private httpClient: HttpClientService) {}

  /**
   * Login with email and password
   * Uses early returns in error handling via HttpClientService
   * @param credentials - Login credentials
   * @returns Observable with login response
   */
  login(credentials: LoginRequestDto): Observable<LoginResponseDto> {
    return this.httpClient.post<LoginResponseDto>(
      `${this.AUTH_BASE_PATH}/login`,
      credentials
    );
  }

  /**
   * Register a new client user
   * Uses early returns in error handling via HttpClientService
   * @param userData - Client registration data
   * @returns Observable with registration response
   */
  registerClient(userData: RegisterClientRequestDto): Observable<RegisterResponseDto> {
    return this.httpClient.post<RegisterResponseDto>(
      `${this.AUTH_BASE_PATH}/register`,
      userData
    );
  }

  /**
   * Register a new establishment
   * Uses early returns in error handling via HttpClientService
   * @param establishmentData - Establishment registration data
   * @returns Observable with registration response
   */
  registerEstablishment(
    establishmentData: RegisterEstablishmentRequestDto
  ): Observable<RegisterResponseDto> {
    return this.httpClient.post<RegisterResponseDto>(
      `${this.AUTH_BASE_PATH}/register-establishment`,
      establishmentData
    );
  }

  /**
   * Request password reset
   * Uses early returns in error handling via HttpClientService
   * @param email - User email address
   * @returns Observable with success response
   */
  requestPasswordReset(email: string): Observable<{ message: string }> {
    const request: PasswordResetRequestDto = { email };
    return this.httpClient.post<{ message: string }>(
      `${this.AUTH_BASE_PATH}/reset-password-request`,
      request
    );
  }

  /**
   * Reset password with token
   * Uses early returns in error handling via HttpClientService
   * @param resetData - Password reset data
   * @returns Observable with success response
   */
  resetPassword(resetData: PasswordResetDto): Observable<{ message: string }> {
    return this.httpClient.post<{ message: string }>(
      `${this.AUTH_BASE_PATH}/reset-password`,
      resetData
    );
  }

  /**
   * Validate JWT token
   * Uses early returns in error handling via HttpClientService
   * @returns Observable with validation result
   */
  validateToken(): Observable<boolean> {
    return this.httpClient.get<{ valid: boolean }>(`${this.AUTH_BASE_PATH}/validate`).pipe(
      map((response) => response.valid)
    );
  }
}
