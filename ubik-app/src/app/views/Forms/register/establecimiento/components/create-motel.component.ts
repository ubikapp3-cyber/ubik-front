import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of, switchMap } from 'rxjs';

import { MotelService } from '../../../../../core/services/register-motel.service';
import { CloudinaryService } from '../../../../../core/services/claudinary.service';
import { CreateMotelRequest, DocumentType } from '../types/register-establishment.types';
import { AuthService } from '../../../../../core/middleware/auth.service';

@Component({
  selector: 'app-create-motel',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-motel.component.html',
})
export class CreateMotelComponent {
  private fb = inject(FormBuilder);

  loading = false;
  error: string | null = null;

  documentTypes: DocumentType[] = ['CC', 'NIT', 'CE', 'PASAPORTE'];

  gettingLocation = false;
  locationStatus: string | null = null;

  motelImages: File[] = [];
  rntFile: File | null = null;
  ruesFile: File | null = null;
  legalDocumentFile: File | null = null;


  form = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    address: ['', [Validators.required]],
    phoneNumber: [''],
    description: [''],
    city: ['', [Validators.required]],

    latitude: [null as number | null],
    longitude: [null as number | null],

    ownerDocumentType: ['CC' as DocumentType, [Validators.required]],
    ownerDocumentNumber: ['', [Validators.required]],
    ownerFullName: ['', [Validators.required]],
    legalRepresentativeName: [''],
  });

  constructor(
    private motelService: MotelService,
    private cloudinary: CloudinaryService,
    private auth: AuthService,
    public router: Router
  ) {}

  getUserLocation() {
    if (!navigator.geolocation) {
      this.locationStatus = 'Geolocalización no soportada.';
      return;
    }
    this.gettingLocation = true;
    this.locationStatus = 'Solicitando permiso...';
    this.error = null;

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.gettingLocation = false;
        const { latitude, longitude } = pos.coords;
        this.form.patchValue({ latitude, longitude });
        this.locationStatus = 'Ubicación obtenida.';
      },
      (err) => {
        this.gettingLocation = false;
        this.locationStatus = null;
        this.error =
          err.code === err.PERMISSION_DENIED ? 'Permiso de ubicación denegado.' :
          err.code === err.POSITION_UNAVAILABLE ? 'Ubicación no disponible.' :
          err.code === err.TIMEOUT ? 'Timeout obteniendo ubicación.' :
          'Error obteniendo ubicación.';
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }

  onMotelImagesSelected(evt: Event) {
    const input = evt.target as HTMLInputElement;
    this.motelImages = input.files ? Array.from(input.files) : [];
  }
  onRntSelected(evt: Event) {
    const input = evt.target as HTMLInputElement;
    this.rntFile = input.files?.[0] ?? null;
  }
  onRuesSelected(evt: Event) {
    const input = evt.target as HTMLInputElement;
    this.ruesFile = input.files?.[0] ?? null;
  }
  onLegalDocSelected(evt: Event) {
    const input = evt.target as HTMLInputElement;
    this.legalDocumentFile = input.files?.[0] ?? null;
  }

  submit() {
    this.error = null;
    const userId = this.auth.user()?.id;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    if (!this.rntFile || !this.ruesFile) {
      this.error = 'Debes subir los archivos de RNT y RUES.';
      return;
    }
    if (!userId) {
      this.loading = false;
      this.error = 'No hay usuario logueado.';
      return; 
    }

    this.loading = true;
    const v = this.form.getRawValue();

    const rnt$ = this.cloudinary.uploadFile(this.rntFile, 'legal');
    const rues$ = this.cloudinary.uploadFile(this.ruesFile, 'legal');

    const images$ = this.motelImages.length
      ? forkJoin(this.motelImages.map((f) => this.cloudinary.uploadFile(f, 'gallery')))
      : of([] as string[]);

    const legalDoc$ = this.legalDocumentFile
      ? this.cloudinary.uploadFile(this.legalDocumentFile, 'legal')
      : of(undefined);

    forkJoin({ rntUrl: rnt$, ruesUrl: rues$, imageUrls: images$, legalUrl: legalDoc$ })
      .pipe(
        switchMap(({ rntUrl, ruesUrl, imageUrls, legalUrl }) => {
          const payload: CreateMotelRequest = {
            name: v.name!,
            address: v.address!,
            phoneNumber: v.phoneNumber || undefined,
            description: v.description || undefined,
            city: v.city!,
            propertyId: userId,

            latitude: v.latitude ?? undefined,
            longitude: v.longitude ?? undefined,

            imageUrls: imageUrls.length ? imageUrls : undefined,

            rnt: rntUrl,
            rues: ruesUrl,

            ownerDocumentType: v.ownerDocumentType!,
            ownerDocumentNumber: v.ownerDocumentNumber!,
            ownerFullName: v.ownerFullName!,
            legalRepresentativeName: v.legalRepresentativeName || undefined,
            legalDocumentUrl: legalUrl,
          };

          return this.motelService.createMotel(payload);
        })
      )
      .subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/listProperty']);
        },
        error: (err) => {
          this.loading = false;
          this.error = err?.error?.message || 'Error creando el motel.';
        },
      });
  }
}
