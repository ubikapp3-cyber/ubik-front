import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { PropertyUserService } from '../../core/services/list-motel.service';
import { Users } from '../../core/models/users.model'; // Idealmente cambia esto a Motel[]
import { Motel } from '../../core/models/motel.model';

@Component({
  selector: 'app-property-user',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './property-user.component.html',
})
export class PropertyUserComponent implements OnInit {
  properties: Motel[] = [];
  loading = false;
  errorMsg: string | null = null;

  constructor(private propertyUserService: PropertyUserService) {}

  ngOnInit() {
    this.loadProperties();
  }

  loadProperties() {
    this.loading = true;
    this.errorMsg = null;

    this.propertyUserService.getMyMotels()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => {
          this.properties = data ?? [];
        },
        error: (err) => {
          // Útil para ver si es 401 (token), 403, 404, etc.
          console.error('Error cargando moteles:', err);
          this.errorMsg = 'No se pudieron cargar tus moteles. Revisa sesión/token.';
        },
      });
  }

  deleteProperty(id: number) {
    if (!confirm('¿Eliminar esta propiedad?')) return;

    this.propertyUserService.deleteProperty(id).subscribe({
      next: () => {
        this.properties = this.properties.filter(p => p.id !== id);
      },
      error: (err) => {
        console.error('Error eliminando motel:', err);
        alert('No se pudo eliminar. Revisa permisos o el endpoint.');
      }
    });
  }

  trackById(_index: number, item: Users) {
    return item.id;
  }
}
