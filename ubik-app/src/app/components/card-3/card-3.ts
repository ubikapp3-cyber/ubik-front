import { Component, Input } from '@angular/core';
import { Button01 } from '../button-01/button-01';
import { Button02 } from '../button-02/button-02';
import { CurrencyPipe } from '@angular/common';

export interface Card3Informacion {
  id: number;
  motelId: number;
  numberHab: string;
  title: string;       
  descripcion: string;
  image: string;
  location: string;   
  adress: string;      
  price: number;
  lat?: number;
  lng?: number;
}

@Component({
  selector: 'app-card-3',
  standalone: true,
  imports: [Button01, Button02, CurrencyPipe],
  templateUrl: './card-3.html',
})
export class Card3 {
  @Input() card!: Card3Informacion;

  @Input() services: {
    id: number;
    name: string;
    icon: string;
  }[] = [];
}
