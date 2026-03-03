import { Component, Input } from '@angular/core';

export interface CardHabitacion {
  id: number;
  nombre: string;
  number: string;
  tipo: string;
  servicios: string[];
  descripcion: string;
  imagen: string;
  price: number;
}

@Component({
  selector: 'app-card-room',
  standalone: true,
  templateUrl: './card-room.html',
})
export class CardRoom {
  @Input() cardHabitacion!: CardHabitacion;
}