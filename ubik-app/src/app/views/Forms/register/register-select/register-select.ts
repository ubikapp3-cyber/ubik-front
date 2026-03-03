import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';


import {
  RegistrationType,
  RegisterWizardState,
  RegisterDataState,
  ClientRegisterData,
  OwnerData,
  EstablishmentInfo
} from './types/register.types';

import { Button01 } from '../../../../components/button-01/button-01';

@Component({
  selector: 'app-register-select',
  standalone: true,
  imports: [CommonModule, Button01],
  templateUrl: './register-select.html',
})
export class RegisterSelect {

  constructor(private router: Router) {}

  // =========================
  // Acciones UI
  // =========================

  onSelectEstablishment(): void {
    console.log('Seleccionó Propetario de Establecimiento');

    this.router.navigate(['/register-propertyEst']);

  }

  onSelectClient(): void {
    console.log('Seleccionó Cliente');

    this.router.navigate(['/register-user']);
  }

  // =========================
  // Helpers (opcional)
  // =========================

  private openEstablishmentDialog(): void {
  }
}
