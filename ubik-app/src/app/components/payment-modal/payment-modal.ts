import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog';
import { Router } from '@angular/router';
import { AuthService } from '../../core/middleware/auth.service';
import { RoomService } from '../../core/services/room.service';
import { Room } from '../../core/models/room.model';
import { PaymentService } from '../../core/services/payment.service';
import { ReservationService } from '../../core/services/reservation.service';

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
  processing = false;
  processingMessage = '';

  constructor(
    private dialogRef: DialogRef<any>,
    @Inject(DIALOG_DATA) public data: { id?: number },
    private roomService: RoomService,
    private auth: AuthService,
    private router: Router,
    private paymentService: PaymentService,
    private reservationService: ReservationService,
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

    this.processing = true;
    this.processingMessage = 'Creando reserva...';

    // Obtener el ID del usuario actual de la sesión/token
    // En este caso asumo que auth.user() o similar tiene un id
    const currentUser = this.auth.user();
    if (!currentUser || !currentUser.id) {
       this.processing = false;
       alert('No se pudo obtener el ID del usuario.');
       return;
    }

    // Calcular fechas (por defecto: hoy a mañana)
    const checkIn = new Date();
    // Sumar un rato para el check in o pasarlo al futuro
    checkIn.setHours(checkIn.getHours() + 1);

    const checkOut = new Date();
    checkOut.setDate(checkOut.getDate() + 1);

    const reservationReq = {
      roomId: this.room.id,
      userId: currentUser.id,
      checkInDate: checkIn.toISOString(),
      checkOutDate: checkOut.toISOString(),
      totalPrice: this.room.price,
      specialRequests: 'Reserva generada automáticamente'
    };

    this.reservationService.createReservation(reservationReq).subscribe({
      next: (reservation) => {
        this.processingMessage = 'Iniciando pago...';
        const paymentReq = {
          reservationId: reservation.id,
          motelId: this.room!.motelId,
          amount: this.room!.price
        };

        this.paymentService.createPayment(paymentReq).subscribe({
          next: (payment) => {
            this.processing = false;
            if (payment.initPoint) {
               // Redirigir a MercadoPago
               window.location.href = payment.initPoint;
            } else {
               alert('No se recibió enlace de pago');
               this.dialogRef.close();
            }
          },
          error: (err) => {
            console.error('Error creando pago', err);
            this.processing = false;
            alert('Error al iniciar el pago.');
          }
        });
      },
      error: (err) => {
        console.error('Error creando reserva', err);
        this.processing = false;
        alert('Error al crear la reserva.');
      }
    });
  }
}
