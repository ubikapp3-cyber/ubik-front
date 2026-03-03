/**
 * Validation utilities for registration forms
 * Each validator follows Single Responsibility Principle
 * Uses early returns to reduce nesting
 */

import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ValidationError } from '../../views/Forms/register/register-user-client/types/register-user.types';

/* =======================
   Constants & Regex
======================= */

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD_LENGTH = 8;

const UPPERCASE_REGEX = /[A-Z]/;
const NUMBER_REGEX = /[0-9]/;
const SPECIAL_CHAR_REGEX = /[^A-Za-z0-9]/;

/* =======================
   Email validation
======================= */

/**
 * Validates email format
 * @returns error message if invalid, null if valid
 */
export function validateEmail(email: string): string | null {
  if (!email) {
    return 'El correo electrónico es requerido';
  }

  if (!EMAIL_REGEX.test(email)) {
    return 'El correo electrónico no es válido';
  }

  return null;
}

/* =======================
   Password validation
======================= */

/**
 * Validates password strength
 * @returns error message if invalid, null if valid
 */
export function validatePassword(password: string): string | null {
  if (!password) {
    return 'La contraseña es requerida';
  }

  if (password.length < MIN_PASSWORD_LENGTH) {
    return `La contraseña debe tener al menos ${MIN_PASSWORD_LENGTH} caracteres`;
  }

  if (!UPPERCASE_REGEX.test(password)) {
    return 'La contraseña debe contener al menos una letra mayúscula';
  }

  if (!NUMBER_REGEX.test(password)) {
    return 'La contraseña debe contener al menos un número';
  }

  if (!SPECIAL_CHAR_REGEX.test(password)) {
    return 'La contraseña debe contener al menos un carácter especial';
  }

  return null;
}

/* =======================
   Password confirmation
======================= */

/**
 * Validates password confirmation matches
 * @returns error message if invalid, null if valid
 */
export function validatePasswordConfirmation(
  password: string,
  confirmPassword: string
): string | null {
  if (!confirmPassword) {
    return 'Debe confirmar la contraseña';
  }

  if (password !== confirmPassword) {
    return 'Las contraseñas no coinciden';
  }

  return null;
}

/* =======================
   Generic field validation
======================= */

/**
 * Validates required text field
 * @returns error message if invalid, null if valid
 */
export function validateRequiredField(
  value: string,
  fieldName: string
): string | null {
  if (!value || value.trim().length === 0) {
    return `${fieldName} es requerido`;
  }

  return null;
}

/* =======================
   Birth date validation
======================= */

/**
 * Validates date of birth (day, month, year)
 * @returns error message if invalid, null if valid
 */
export function validateBirthDate(
  day: string,
  month: string,
  year: string
): string | null {
  if (!day || !month || !year) {
    return 'La fecha de nacimiento es requerida';
  }

  const dayNum = parseInt(day, 10);
  const monthNum = parseInt(month, 10);
  const yearNum = parseInt(year, 10);

  if (isNaN(dayNum) || dayNum < 1 || dayNum > 31) {
    return 'El día debe estar entre 1 y 31';
  }

  if (isNaN(monthNum) || monthNum < 1 || monthNum > 12) {
    return 'El mes debe estar entre 1 y 12';
  }

  const currentYear = new Date().getFullYear();
  if (isNaN(yearNum) || yearNum < 1900 || yearNum > currentYear) {
    return `El año debe estar entre 1900 y ${currentYear}`;
  }

  const date = new Date(yearNum, monthNum - 1, dayNum);
  if (
    date.getDate() !== dayNum ||
    date.getMonth() !== monthNum - 1 ||
    date.getFullYear() !== yearNum
  ) {
    return 'La fecha no es válida';
  }

  const today = new Date();
  const birthDate = new Date(yearNum, monthNum - 1, dayNum);

  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();

  if (
    monthDiff < 0 ||
    (monthDiff === 0 && today.getDate() < birthDate.getDate())
  ) {
    age--;
  }

  if (age < 18) {
    return 'Debe ser mayor de 18 años';
  }

  return null;
}

/* =======================
   File upload validation
======================= */

/**
 * Validates file upload
 * @returns error message if invalid, null if valid
 */
export function validateFileUpload(
  file: File | null,
  fieldName: string
): string | null {
  if (!file) {
    return `${fieldName} es requerido`;
  }

  const maxSize = 5 * 1024 * 1024; // 5MB
  if (file.size > maxSize) {
    return `${fieldName} no debe superar los 5MB`;
  }

  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg'];
  if (!allowedTypes.includes(file.type)) {
    return `${fieldName} debe ser una imagen JPG o PNG`;
  }

  return null;
}

/* =======================
   Error collector
======================= */

/**
 * Collects all validation errors into an array
 * Uses early returns to exit as soon as errors are found if stopOnFirstError is true
 */
export function collectValidationErrors(
  validations: Array<{ field: string; validator: () => string | null }>,
  stopOnFirstError = false
): ValidationError[] {
  const errors: ValidationError[] = [];

  for (const { field, validator } of validations) {
    const error = validator();
    if (error) {
      errors.push({ field, message: error });

      if (stopOnFirstError) {
        return errors;
      }
    }
  }

  return errors;
}

/**
  Verifica el email verdadero de la cuenta registrada en la base de datos
 */

export function toValidatorFn(
  fn: (value: string) => string | null,
    errorKey: string
  ): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const error = fn(control.value);
      return error ? { [errorKey]: error } : null;
    };
}
