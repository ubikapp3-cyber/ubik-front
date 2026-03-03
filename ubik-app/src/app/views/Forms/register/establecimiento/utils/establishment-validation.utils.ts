import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class EstablishmentValidation {

  /* ================================
   *  VALIDACIONES BÁSICAS
   * ================================ */

  static required(control: AbstractControl): ValidationErrors | null {
    return control.value === null ||
      control.value === undefined ||
      control.value === ''
      ? { required: true }
      : null;
  }

  static minLength(min: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      return control.value.length < min
        ? { minLength: { requiredLength: min } }
        : null;
    };
  }

  /* ================================
   *  TELÉFONO
   * ================================ */

  static phoneNumber(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;

    const regex = /^\+?\d{7,15}$/;
    return !regex.test(control.value)
      ? { phoneNumber: true }
      : null;
  }

  /* ================================
   *  EMAIL
   * ================================ */

  static email(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;

    const regex =
      /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    return !regex.test(control.value)
      ? { email: true }
      : null;
  }

  /* ================================
   *  COORDENADAS
   * ================================ */

  static latitude(control: AbstractControl): ValidationErrors | null {
    if (control.value === null || control.value === '') return null;

    const value = Number(control.value);
    return isNaN(value) || value < -90 || value > 90
      ? { latitude: true }
      : null;
  }

  static longitude(control: AbstractControl): ValidationErrors | null {
    if (control.value === null || control.value === '') return null;

    const value = Number(control.value);
    return isNaN(value) || value < -180 || value > 180
      ? { longitude: true }
      : null;
  }

  /* ================================
   *  IMÁGENES
   * ================================ */

  static imagesRequired(min = 1): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const files = control.value as File[];

      if (!files || files.length < min) {
        return { imagesRequired: { min } };
      }
      return null;
    };
  }

  static imageSize(maxMb = 5): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const files = control.value as File[];
      if (!files) return null;

      const maxSize = maxMb * 1024 * 1024;
      const invalid = files.some(file => file.size > maxSize);

      return invalid ? { imageSize: true } : null;
    };
  }

  static imageType(
    allowedTypes: string[] = ['image/jpeg', 'image/png', 'image/webp']
  ): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const files = control.value as File[];
      if (!files) return null;

      const invalid = files.some(
        file => !allowedTypes.includes(file.type)
      );

      return invalid ? { imageType: true } : null;
    };
  }

  /* ================================
   *  PASSWORDS (LocationInfo)
   * ================================ */

  static strongPassword(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;

    const regex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

    return !regex.test(control.value)
      ? { strongPassword: true }
      : null;
  }

  static matchFields(
    field: string,
    confirmField: string
  ): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const value = group.get(field)?.value;
      const confirm = group.get(confirmField)?.value;

      if (value !== confirm) {
        group.get(confirmField)?.setErrors({ match: true });
        return { match: true };
      }

      return null;
    };
  }
}