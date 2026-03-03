import { Component, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ForgotService } from '../../../core/services/forgot.service';
import { toValidatorFn, validateEmail, validatePassword } from '../../../core/utils/validation.utils';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './forgot-password.component.html',
})
export class ForgotPasswordComponent {
  step = signal(1);
  loading = signal(false);
  error = signal<string | null>(null);
  success = signal<string | null>(null); // ✅ NUEVO

  private fb = new FormBuilder();

  constructor(private forgotService: ForgotService) {}

  form = this.fb.nonNullable.group({
    email: ['', [toValidatorFn(validateEmail, 'email')]],
    token: ['', [Validators.required]],
    newPassword: ['', [toValidatorFn(validatePassword, 'newPassword')]],
  });

  sendEmail() {
    const emailCtrl = this.form.controls.email;
    emailCtrl.markAsTouched();

    if (emailCtrl.invalid || this.loading()) return;

    this.loading.set(true);
    this.error.set(null);
    this.success.set(null); // ✅ limpiar éxito

    this.forgotService.requestReset(emailCtrl.value).subscribe({
      next: (res) => {
        console.log('requestReset OK', res);
        this.step.set(2);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('requestReset ERROR FULL:', err);
        this.loading.set(false);
        this.error.set('No se pudo enviar el correo');
      }
    });
  }

  resetPassword() {
    const tokenCtrl = this.form.controls.token;
    const passCtrl = this.form.controls.newPassword;

    tokenCtrl.markAsTouched();
    passCtrl.markAsTouched();

    const token = tokenCtrl.value.trim();
    const newPassword = passCtrl.value.trim();
    this.form.patchValue({ token, newPassword });

    if (tokenCtrl.invalid || passCtrl.invalid) {
      this.success.set(null);
      this.error.set('Revisa el token y la contraseña (no cumplen validación).');
      return;
    }

    if (this.loading()) return;

    this.loading.set(true);
    this.error.set(null);
    this.success.set(null); // ✅ limpiar éxito antes de enviar

    this.forgotService.resetPassword(token, newPassword).subscribe({
      next: (res) => {
        console.log('resetPassword OK', res);
        this.loading.set(false);
        this.error.set(null);
        this.success.set('¡Listo! Tu contraseña fue cambiada. Ya puedes iniciar sesión.'); // ✅ AVISO OK
        // Opcional: limpiar campos
        // this.form.patchValue({ token: '', newPassword: '' });
      },
      error: (err) => {
        console.error('resetPassword ERROR FULL:', err);
        this.loading.set(false);
        this.success.set(null);
        this.error.set('No se pudo cambiar la contraseña. Token inválido/expirado o contraseña no válida.'); // ✅ AVISO FAIL
      }
    });
  }
}