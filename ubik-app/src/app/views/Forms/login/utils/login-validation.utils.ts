/**
 * Validation utilities for login forms
 * Each validator follows Single Responsibility Principle (SRP)
 * Uses early returns to reduce nesting
 */

import { ValidationError, LoginFormData } from '../types/login.types';

/**
 * Validates username
 * - Required
 * - Trimmed
 * @param username - Username to validate
 * @returns error message if invalid, null if valid
 */
export function validateUsername(username: string): string | null {
  if (!username || username.trim().length === 0) {
    return 'El usuario es requerido';
  }

  return null;
}

/**
 * Validates password
 * - Required
 * @param password - Password to validate
 * @returns error message if invalid, null if valid
 */
export function validatePassword(password: string): string | null {
  if (!password || password.trim().length === 0) {
    return 'La contraseña es requerida';
  }

  return null;
}

/**
 * Validates entire login form
 * Collects all validation errors
 * @param data - Form data to validate
 * @returns array of validation errors (empty if valid)
 */
export function validateLoginForm(
  data: Partial<LoginFormData>
): ValidationError[] {
  if (!data) {
    return [{ field: 'form', message: 'Datos de formulario inválidos' }];
  }

  const errors: ValidationError[] = [];

  // Username validation
  const usernameError = validateUsername(data.username || '');
  if (usernameError) {
    errors.push({
      field: 'username',
      message: usernameError,
    });
  }

  // Password validation
  const passwordError = validatePassword(data.password || '');
  if (passwordError) {
    errors.push({
      field: 'password',
      message: passwordError,
    });
  }

  return errors;
}

/**
 * Checks if a specific field has an error
 * @param errors - Array of validation errors
 * @param field - Field name to check
 */
export function hasFieldError(
  errors: ValidationError[],
  field: string
): boolean {
  return errors.some(error => error.field === field);
}

/**
 * Gets error message for a specific field
 * @param errors - Array of validation errors
 * @param field - Field name to get error for
 */
export function getFieldError(
  errors: ValidationError[],
  field: string
): string | null {
  if (!errors || errors.length === 0) {
    return null;
  }

  const error = errors.find(e => e.field === field);
  return error ? error.message : null;
}