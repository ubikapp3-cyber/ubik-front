import { Component, OnInit } from '@angular/core';
import { CardOff } from '../../components/card-offers/card-offers'; // Cambia a CardOff
import { CardOffers } from '../../components/card-offers/card-offers'; // El componente
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-rooms-offerts',
  imports: [CardOffers],
  templateUrl: './rooms-offerts.html',
})
export class RoomsOfferts implements OnInit {
  habitacionesOfertas: CardOff[] = []; // Usa CardOff para el tipado
  
  ngOnInit(): void {
    this.cargarHabitaciones();
  }
  
  cargarHabitaciones(): void {
    this.habitacionesOfertas = [
      {
        id: 1,
        image: 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c',
        nombre: 'Oferta Suite Premium',
        descripcion: 'Descripción de la oferta' 
      }
    ];
  }
}