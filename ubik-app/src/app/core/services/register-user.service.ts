import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

import { ValidationError } from '../../views/Forms/register/register-user-client/types/register-user.types';
import { RegisterFormData } from '../../views/Forms/register/register-user-client/types/register-user.types';
import { RegisterUserPayload } from '../../views/Forms/register/register-user-client/types/register-user-payload.types';

import {
  validateEmail,
  validatePassword,
  validatePasswordConfirmation,
  validateRequiredField,
  collectValidationErrors,
} from '../utils/validation.utils';

import { environment } from '../../../environments/environment';

export interface RegistrationResult {
  success: boolean;
  message: string;
  userId?: string;
}

@Injectable({ providedIn: 'root' })
export class RegisterService {

  private readonly REGISTER_URL = `${environment.apiUrl}/auth/register`;

  constructor(private http: HttpClient) {}

  /** VALIDACIÓN SOLO FRONTEND */
  validateClientForm(data: Partial<RegisterFormData>): ValidationError[] {
    if (!data) {
      return [{ field: 'form', message: 'Datos inválidos' }];
    }

    const validations = [
      {
        field: 'username',
        validator: () =>
          validateRequiredField(data.username || '', 'El nombre de usuario'),
      },
      {
        field: 'email',
        validator: () => validateEmail(data.email || ''),
      },
      {
        field: 'password',
        validator: () => validatePassword(data.password || ''),
      },
      {
        field: 'confirmPassword',
        validator: () =>
          validatePasswordConfirmation(
            data.password || '',
            data.comfirmPassword || ''   
          ),
      },
    ];

    return collectValidationErrors(validations);
  }

  /** ENVÍO AL BACKEND */
  submitClientRegistration(data: RegisterFormData): Observable<string> {

    const payload: RegisterFormData = {
      username: data.username?.trim(),
      email: data.email?.trim(),
      password: data.password,
      comfirmPassword: data.comfirmPassword?.trim(),
      phoneNumber: data.phoneNumber?.trim(),
      anonymous: data.anonymous ?? false,
      roleId: data.roleId ?? 3,
      birthDate: data.birthDate,
      latitude: data.latitude ?? 4.6097,
      longitude: data.longitude ?? -74.0721
    };

    console.log('🚀 REGISTER PAYLOAD', payload);

    return this.http.post(
      this.REGISTER_URL,
      payload,
      { responseType: 'text' }
    );
  }
}
