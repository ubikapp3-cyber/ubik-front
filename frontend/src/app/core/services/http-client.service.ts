/**
 * HTTP Client Service
 * Follows Single Responsibility Principle (SRP) - handles HTTP communication only
 * Follows Open/Closed Principle (OCP) - extensible for new endpoints
 * Follows Dependency Inversion Principle (DIP) - depends on HttpClient abstraction
 * 
 * Provides a centralized service for all HTTP requests with:
 * - Consistent error handling
 * - Request/response transformation
 * - Type safety
 */

import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EnvironmentService } from '../config/environment.config';

/**
 * Generic API error interface
 */
export interface ApiError {
  message: string;
  status: number;
  error?: string;
  details?: any;
}

/**
 * HTTP error messages configuration
 * Externalized for easy maintenance and i18n support
 */
const HTTP_ERROR_MESSAGES: Record<number, string> = {
  0: 'No se pudo conectar al servidor. Verifique su conexión.',
  400: 'Solicitud inválida. Verifique los datos ingresados.',
  401: 'No autorizado. Por favor, inicie sesión.',
  403: 'Acceso denegado.',
  404: 'Recurso no encontrado.',
  500: 'Error interno del servidor.',
  503: 'Servicio no disponible. Intente más tarde.',
};

@Injectable({
  providedIn: 'root',
})
export class HttpClientService {
  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {}

  /**
   * Perform GET request
   * Uses early returns for error handling
   * @param endpoint - API endpoint path
   * @returns Observable with response data
   */
  get<T>(endpoint: string): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.get<T>(url).pipe(
      catchError((error) => this.handleError(error))
    );
  }

  /**
   * Perform POST request
   * Uses early returns for error handling
   * @param endpoint - API endpoint path
   * @param body - Request body
   * @returns Observable with response data
   */
  post<T>(endpoint: string, body: any): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.post<T>(url, body).pipe(
      catchError((error) => this.handleError(error))
    );
  }

  /**
   * Perform PUT request
   * Uses early returns for error handling
   * @param endpoint - API endpoint path
   * @param body - Request body
   * @returns Observable with response data
   */
  put<T>(endpoint: string, body: any): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.put<T>(url, body).pipe(
      catchError((error) => this.handleError(error))
    );
  }

  /**
   * Perform DELETE request
   * Uses early returns for error handling
   * @param endpoint - API endpoint path
   * @returns Observable with response data
   */
  delete<T>(endpoint: string): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.delete<T>(url).pipe(
      catchError((error) => this.handleError(error))
    );
  }

  /**
   * Perform PATCH request
   * Uses early returns for error handling
   * @param endpoint - API endpoint path
   * @param body - Request body
   * @returns Observable with response data
   */
  patch<T>(endpoint: string, body: any): Observable<T> {
    const url = this.buildUrl(endpoint);
    return this.http.patch<T>(url, body).pipe(
      catchError((error) => this.handleError(error))
    );
  }

  /**
   * Build full URL from endpoint
   * Uses early returns for validation
   * @param endpoint - API endpoint path
   * @returns Full URL
   */
  private buildUrl(endpoint: string): string {
    // Early return if endpoint is already a full URL
    if (endpoint.startsWith('http://') || endpoint.startsWith('https://')) {
      return endpoint;
    }

    // Determine base URL based on endpoint
    const baseUrl = this.getBaseUrlForEndpoint(endpoint);

    // Remove leading slash from endpoint if present
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint.substring(1) : endpoint;

    // Ensure base URL ends with slash
    const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl : `${baseUrl}/`;

    return `${cleanBaseUrl}${cleanEndpoint}`;
  }

  /**
   * Get appropriate base URL for endpoint
   * Uses early returns for clarity
   * @param endpoint - API endpoint path
   * @returns Base URL
   */
  private getBaseUrlForEndpoint(endpoint: string): string {
    // Auth endpoints use auth API URL
    if (endpoint.includes('/auth/')) {
      return this.environmentService.getAuthApiUrl();
    }

    // Motel endpoints use motel API URL
    if (endpoint.includes('/motel/')) {
      return this.environmentService.getMotelApiUrl();
    }

    // Default to general API base URL
    return this.environmentService.getApiBaseUrl();
  }

  /**
   * Handle HTTP errors
   * Transforms HttpErrorResponse to ApiError
   * Uses early returns for different error types
   * @param error - HTTP error response
   * @returns Observable that throws ApiError
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let apiError: ApiError;

    // Early return for client-side or network error
    if (error.error instanceof ErrorEvent) {
      apiError = {
        message: error.error.message || 'Error de conexión',
        status: 0,
        error: 'Network Error',
      };
      return throwError(() => apiError);
    }

    // Server-side error
    apiError = {
      message: this.extractErrorMessage(error),
      status: error.status,
      error: error.error?.error || error.statusText,
      details: error.error,
    };

    return throwError(() => apiError);
  }

  /**
   * Extract user-friendly error message from HTTP error
   * Uses early returns for different error scenarios
   * Uses externalized error messages for maintainability
   * @param error - HTTP error response
   * @returns User-friendly error message
   */
  private extractErrorMessage(error: HttpErrorResponse): string {
    // Early return for custom backend error message
    if (error.error?.message) {
      return error.error.message;
    }

    // Early return for error string
    if (typeof error.error === 'string') {
      return error.error;
    }

    // Get message from configuration or use default
    return HTTP_ERROR_MESSAGES[error.status] || `Error del servidor (${error.status})`;
  }
}
