import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Inputcomponent } from '../../components/input/input';
import { Button01 } from '../../components/button-01/button-01';
import { Motel } from '../../core/models/motel.model';
import { MotelService } from '../../core/services/motel.service';

@Component({
  selector: 'app-edit-profile-motel',
  standalone: true,
  imports: [CommonModule, Inputcomponent, Button01],
  templateUrl: './edit-profile-motel.html',
})
export class EditProfileMotel implements OnInit {

  private motelService = inject(MotelService);

  profile!: Motel;
  loading = false;
  error: string | null = null;
  success = false;

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading = true;
    this.error = null;

    this.motelService.getProfile().subscribe({
      next: (data: Motel) => {
        this.profile = structuredClone(data);
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Error cargando perfil', err);
        this.error = 'No se pudo cargar el perfil';
        this.loading = false;
      },
    });
  }

  saveProfile(): void {
    this.loading = true;
    this.success = false;
    this.error = null;

    this.motelService.updateProfile(this.profile).subscribe({
      next: (updated: Motel) => {
        this.profile = structuredClone(updated);
        this.success = true;
        this.loading = false;
      },
      error: (err: unknown) => {
        console.error('Error guardando perfil', err);
        this.error = 'No se pudo guardar el perfil';
        this.loading = false;
      },
    });
  }
}
