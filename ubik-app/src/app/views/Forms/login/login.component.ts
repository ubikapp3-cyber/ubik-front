import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { LoginService } from '../../../core/services/login.service';
import { LoginFormData, ValidationError } from './types/login.types';
import { FormsModule } from '@angular/forms';
import { Inputcomponent } from '../../../components/input/input';
import { AuthService } from '../../../core/middleware/auth.service';
import { Button01 } from '../../../components/button-01/button-01';
import { validateLoginForm } from './utils/login-validation.utils';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, Inputcomponent],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  formData = signal<Partial<LoginFormData>>({
    username: '',
    password: '',
  });

  showPassword = signal(false);

  togglePassword() {
    this.showPassword.update((v) => !v);
  }

  errors = signal<ValidationError[]>([]);
  isSubmitting = signal(false);
  rememberMe = signal(false);

  constructor(
    private loginService: LoginService,
    private auth: AuthService,
    private router: Router,
  ) {}

  /* =======================
     FORM UPDATES
     ======================= */

  updateField(field: keyof LoginFormData, value: string): void {
    this.formData.set({
      ...this.formData(),
      [field]: value,
    });
  }

  onUsernameInput(event: Event): void {
    this.updateField('username', (event.target as HTMLInputElement).value);
  }

  onPasswordInput(event: Event): void {
    this.updateField('password', (event.target as HTMLInputElement).value);
  }

  onRememberMeChange(event: Event): void {
    this.rememberMe.set((event.target as HTMLInputElement).checked);
  }

  /* =======================
     SUBMIT
     ======================= */

  onFormSubmit(): void {
    const data = this.formData();

    // ✅ 1) Validación front: llena errores por campo
    const validationErrors = validateLoginForm(data);
    this.errors.set(validationErrors);

    if (validationErrors.length > 0) {
      return;
    }

    this.isSubmitting.set(true);

    this.loginService
      .login(
        {
          username: data.username!, // ya pasó validación
          password: data.password!,
        },
        this.rememberMe(),
      )
      .subscribe({
        next: () => {
          this.loginService.getProfile().subscribe({
            next: () => {
              this.isSubmitting.set(false);
              this.errors.set([]); // ✅ limpia errores al éxito
              this.router.navigate(['/']);
            },
            error: (err: any) => {
              console.error('Error cargando perfil', err);
              this.isSubmitting.set(false);
              this.errors.set([
                { field: 'form', message: 'Iniciaste sesión, pero no se pudo cargar tu perfil.' },
              ]);
            },
          });
        },
        error: (err: any) => {
          console.error('Error login', err);
          this.isSubmitting.set(false);

          // ✅ 2) Error backend (fallback seguro)
          const status = err?.status;
          const apiMsg = err?.error?.message || err?.message;

          // Si tu API NO diferencia usuario/password (lo normal), muestra genérico:
          const msg = 'Credenciales incorrectas'

          this.errors.set([{ field: 'form', message: msg }]);

          // Si quieres que salga debajo de contraseña en vez de arriba:
          // this.errors.set([{ field: 'password', message: msg }]);
        },
      });
    }
  /* =======================
     ERRORS
     ======================= */

  hasFieldError(field: string): boolean {
    return this.errors().some((e) => e.field === field);
  }

  getFieldError(field: string): string | null {
    return this.errors().find((e) => e.field === field)?.message || null;
  }

  /* =======================
     NAVIGATION
     ======================= */

  navigateToRegister(): void {
    this.router.navigate(['/']);
  }

  navigateToPasswordReset(): void {
    this.router.navigate(['/forgot-password']); // Recuperacion de contraseña
  }
}
