import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Dialog, DialogModule } from '@angular/cdk/dialog';
import { PaymentModal } from '../payment-modal/payment-modal';

@Component({
  selector: 'app-button-01',
  standalone: true,
  imports: [CommonModule, DialogModule],
  templateUrl: './button-01.html',
})
export class Button01 {
  @Input() text!: string;
  @Input() subtext?: string;
  @Input() routerLink?: string | (string | number)[];
  @Input() iconLeft?: string;
  @Input() iconRight?: string;
  @Input() id!: number;
  @Input() action!: 'reservar' | 'detalles';
  @Input() disabled = false;
  @Input() type!: 'submit' | 'button';

  // si es true el botón ocupará el 100%
  @Input() fullWidth: boolean = false;

  // si es true el botón tendrá mayor altura y tamaño de texto
  @Input() tall: boolean = false;

  constructor(
    private router: Router,
    private dialog: Dialog,
  ) {}

  navigate() {
    // Si la acción es reservar, abrimos el modal de pago con la habitación seleccionada
    if (this.action === 'reservar') {
      if (this.id == null) return;
      this.dialog.open(PaymentModal, { data: { id: this.id } });
      return;
    }

    if (!this.routerLink) return;

    if (Array.isArray(this.routerLink)) {
      this.router.navigate(this.routerLink);
    } else {
      this.router.navigate([this.routerLink]);
    }
  }
}
