/**
 * Registration API Service
 * Follows Single Responsibility Principle (SRP) - handles registration API communication only
 * Follows Dependency Inversion Principle (DIP) - depends on HttpClientService abstraction
 * 
 * Provides methods for all registration-related API calls
 */

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClientService } from '../../../../core/services/http-client.service';
import {
  RegisterClientRequestDto,
  RegisterEstablishmentRequestDto,
  RegisterResponseDto,
} from '../../../../core/models/api.models';
import { AuthApiService } from '../../login/services/auth-api.service';

@Injectable({
  providedIn: 'root',
})
export class RegistrationApiService {
  constructor(
    private httpClient: HttpClientService,
    private authApiService: AuthApiService
  ) {}

  /**
   * Register a new client user
   * Uses early returns in error handling via HttpClientService
   * @param userData - Client registration data
   * @returns Observable with registration response
   */
  registerClient(userData: RegisterClientRequestDto): Observable<RegisterResponseDto> {
    return this.authApiService.registerClient(userData);
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
    return this.authApiService.registerEstablishment(establishmentData);
  }

  /**
   * Upload file to server
   * Uses early returns for validation
   * TODO: Implement when file upload endpoint is available
   * @param file - File to upload
   * @param type - Type of file (e.g., 'id-front', 'id-back', 'establishment-image')
   * @returns Observable with upload response
   */
  uploadFile(file: File, type: string): Observable<{ url: string }> {
    // For now, files are handled within the registration request
    // This method is a placeholder for future file upload implementation
    return new Observable((observer) => {
      observer.next({ url: `mock-url-${file.name}` });
      observer.complete();
    });
  }
}
