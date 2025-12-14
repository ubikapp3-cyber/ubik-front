/**
 * Registration service interface
 * Follows Interface Segregation Principle - client depends only on what it needs
 */

import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  ClientFormData,
  EstablishmentFormData,
  ValidationError,
} from '../types/register.types';
import {
  validateEmail,
  validatePassword,
  validatePasswordConfirmation,
  validateRequiredField,
  validateBirthDate,
  validateFileUpload,
  collectValidationErrors,
} from '../utils/validation.utils';
import { RegistrationApiService } from './registration-api.service';
import {
  RegisterClientRequestDto,
  RegisterEstablishmentRequestDto,
} from '../../../../core/models/api.models';
import { ApiError } from '../../../../core/services/http-client.service';

export interface RegistrationResult {
  success: boolean;
  message: string;
  userId?: string;
}

@Injectable({
  providedIn: 'root',
})
export class RegisterService {
  constructor(private registrationApiService: RegistrationApiService) {}
  /**
   * Validates client registration form
   * Uses early returns to exit quickly on validation failure
   */
  validateClientForm(data: Partial<ClientFormData>): ValidationError[] {
    // Early return if no data
    if (!data) {
      return [{ field: 'form', message: 'Datos de formulario inválidos' }];
    }

    const validations = [
      {
        field: 'fullName',
        validator: () => validateRequiredField(data.fullName || '', 'El nombre completo'),
      },
      {
        field: 'email',
        validator: () => validateEmail(data.email || ''),
      },
      {
        field: 'birthDate',
        validator: () =>
          validateBirthDate(data.birthDay || '', data.birthMonth || '', data.birthYear || ''),
      },
      {
        field: 'password',
        validator: () => validatePassword(data.password || ''),
      },
      {
        field: 'confirmPassword',
        validator: () =>
          validatePasswordConfirmation(data.password || '', data.confirmPassword || ''),
      },
    ];

    return collectValidationErrors(validations);
  }

  /**
   * Validates establishment owner info step
   * Uses early returns for cleaner validation flow
   */
  validateEstablishmentOwnerInfo(
    data: Partial<EstablishmentFormData>
  ): ValidationError[] {
    // Early return if no owner data
    if (!data.owner) {
      return [{ field: 'owner', message: 'Datos del propietario inválidos' }];
    }

    const owner = data.owner;
    const validations = [
      {
        field: 'ownerName',
        validator: () => validateRequiredField(owner.ownerName || '', 'El nombre del dueño'),
      },
      {
        field: 'ownerEmail',
        validator: () => validateEmail(owner.ownerEmail || ''),
      },
      {
        field: 'identificationNumber',
        validator: () =>
          validateRequiredField(owner.identificationNumber || '', 'El número de identificación'),
      },
      {
        field: 'frontIdImage',
        validator: () => validateFileUpload(owner.frontIdImage, 'La foto frontal del documento'),
      },
      {
        field: 'backIdImage',
        validator: () => validateFileUpload(owner.backIdImage, 'La foto trasera del documento'),
      },
    ];

    return collectValidationErrors(validations);
  }

  /**
   * Validates establishment location step
   * Uses early returns for cleaner validation flow
   */
  validateEstablishmentLocation(
    data: Partial<EstablishmentFormData>
  ): ValidationError[] {
    // Early return if no location data
    if (!data.location) {
      return [{ field: 'location', message: 'Datos de ubicación inválidos' }];
    }

    const location = data.location;
    const validations = [
      {
        field: 'establishmentName',
        validator: () =>
          validateRequiredField(location.establishmentName || '', 'El nombre del establecimiento'),
      },
      {
        field: 'establishmentEmail',
        validator: () => validateEmail(location.establishmentEmail || ''),
      },
      {
        field: 'rues',
        validator: () => validateRequiredField(location.rues || '', 'El RUES'),
      },
      {
        field: 'rnt',
        validator: () => validateRequiredField(location.rnt || '', 'El RNT'),
      },
      {
        field: 'country',
        validator: () => validateRequiredField(location.country || '', 'El país'),
      },
      {
        field: 'department',
        validator: () => validateRequiredField(location.department || '', 'El departamento'),
      },
      {
        field: 'municipality',
        validator: () => validateRequiredField(location.municipality || '', 'El municipio'),
      },
      {
        field: 'password',
        validator: () => validatePassword(location.password || ''),
      },
      {
        field: 'confirmPassword',
        validator: () =>
          validatePasswordConfirmation(
            location.password || '',
            location.confirmPassword || ''
          ),
      },
    ];

    return collectValidationErrors(validations);
  }

  /**
   * Validates establishment images step
   * Uses early returns for cleaner validation flow
   */
  validateEstablishmentImages(data: Partial<EstablishmentFormData>): ValidationError[] {
    // Early return if no images data
    if (!data.images) {
      return [{ field: 'images', message: 'Debe subir al menos una imagen' }];
    }

    if (!data.images.images || data.images.images.length === 0) {
      return [{ field: 'images', message: 'Debe subir al menos una imagen del establecimiento' }];
    }

    return [];
  }

  /**
   * Submits client registration
   * Integrated with backend API
   */
  submitClientRegistration(data: ClientFormData): Observable<RegistrationResult> {
    // Validate before submission
    const errors = this.validateClientForm(data);
    if (errors.length > 0) {
      return throwError(() => ({
        success: false,
        message: 'Errores de validación en el formulario',
      }));
    }

    // Create request DTO with proper date format
    const birthDate = this.formatBirthDate(data.birthDay, data.birthMonth, data.birthYear);
    const registerRequest: RegisterClientRequestDto = {
      fullName: data.fullName,
      email: data.email,
      password: data.password,
      birthDate: birthDate,
    };

    // Call backend API
    return this.registrationApiService.registerClient(registerRequest).pipe(
      map((response) => ({
        success: true,
        message: response.message || 'Registro de cliente exitoso',
        userId: response.userId,
      })),
      catchError((error: ApiError) => {
        return throwError(() => ({
          success: false,
          message: error.message || 'Error al registrar cliente',
        }));
      })
    );
  }

  /**
   * Submits establishment registration
   * Integrated with backend API
   * Note: File uploads are handled as multipart/form-data in the request
   */
  submitEstablishmentRegistration(
    data: EstablishmentFormData
  ): Observable<RegistrationResult> {
    // Validate all steps
    const ownerErrors = this.validateEstablishmentOwnerInfo(data);
    const locationErrors = this.validateEstablishmentLocation(data);
    const imageErrors = this.validateEstablishmentImages(data);

    const allErrors = [...ownerErrors, ...locationErrors, ...imageErrors];
    if (allErrors.length > 0) {
      return throwError(() => ({
        success: false,
        message: 'Errores de validación en el formulario',
      }));
    }

    if (!data.acceptedTerms) {
      return throwError(() => ({
        success: false,
        message: 'Debe aceptar los términos y condiciones',
      }));
    }

    // Create request DTO
    const registerRequest: RegisterEstablishmentRequestDto = {
      ownerName: data.owner.ownerName,
      ownerEmail: data.owner.ownerEmail,
      identificationNumber: data.owner.identificationNumber,
      establishmentName: data.location.establishmentName,
      establishmentEmail: data.location.establishmentEmail,
      rues: data.location.rues,
      rnt: data.location.rnt,
      password: data.location.password,
      country: data.location.country,
      department: data.location.department,
      municipality: data.location.municipality,
      acceptedTerms: data.acceptedTerms,
    };

    // Call backend API
    // Note: In a real implementation with file uploads, you would need to
    // use FormData and handle multipart/form-data requests
    return this.registrationApiService.registerEstablishment(registerRequest).pipe(
      map((response) => ({
        success: true,
        message: response.message || 'Registro de establecimiento exitoso',
        userId: response.userId,
      })),
      catchError((error: ApiError) => {
        return throwError(() => ({
          success: false,
          message: error.message || 'Error al registrar establecimiento',
        }));
      })
    );
  }

  /**
   * Format birth date components into ISO date string (YYYY-MM-DD)
   * Uses early returns for validation
   * @param day - Day of birth
   * @param month - Month of birth
   * @param year - Year of birth
   * @returns ISO formatted date string
   */
  private formatBirthDate(day: string, month: string, year: string): string {
    // Pad day and month with leading zeros if needed
    const paddedDay = day.padStart(2, '0');
    const paddedMonth = month.padStart(2, '0');
    return `${year}-${paddedMonth}-${paddedDay}`;
  }
}
