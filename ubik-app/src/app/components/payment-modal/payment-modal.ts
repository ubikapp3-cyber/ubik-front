import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog';
import { Router } from '@angular/router';
import { AuthService } from '../../core/middleware/auth.service';
import { RoomService } from '../../core/services/room.service';
import { Room } from '../../core/models/room.model';

@Component({
  selector: 'app-payment-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-modal.html',
})
export class PaymentModal implements OnInit {
  room: Room | null = null;
  loading = true;
  error = false;

  constructor(
    private dialogRef: DialogRef<any>,
    @Inject(DIALOG_DATA) public data: { id?: number },
    private roomService: RoomService,
    private auth: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const id = this.data?.id;
    if (!id) {
      this.loading = false;
      this.error = true;
      return;
    }

    this.roomService.getRooms().subscribe({
      next: (rooms: Room[]) => {
        this.room = rooms.find((r) => r.id === id) ?? null;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      },
    });
  }

  close(result?: any) {
    this.dialogRef.close(result);
  }

  reserve() {
    if (!this.room) return;

    // Si no está logueado o no es rol usuario (id 3), abrir login
    if (!this.auth.isLogged()) {
      this.dialogRef.close();
      this.router.navigate(['/login'], { queryParams: { id: this.room.id, from: 'reserve' } });
      return;
    }

    if (!this.auth.isUser()) {
      this.dialogRef.close();
      // usuario logueado pero sin rol cliente
      alert('Necesitas una cuenta de cliente para reservar.');
      return;
    }

    // Usuario válido: proceder (aquí se integrará Mercado Pago en el siguiente paso)
    this.dialogRef.close({ reserved: true, roomId: this.room.id });
  }
}
