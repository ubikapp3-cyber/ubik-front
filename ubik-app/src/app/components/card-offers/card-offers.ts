import { Component, Input } from '@angular/core';
import { Button01 } from '../../components/button-01/button-01';
import { Button02 } from '../../components/button-02/button-02';

export interface CardOff {
  id: number;
  image: string;
  nombre: string;
  descripcion?: string;
}

@Component({
  selector: 'app-card-offers',
  standalone: true,
  imports: [Button01, Button02],
  templateUrl: './card-offers.html',
})
export class CardOffers {

  @Input() card: CardOff = { 
    id: 0,
    image: '',
    nombre: '',
    descripcion: '',
  };

  @Input() showDescription: boolean = true;
  @Input() showButton01: boolean = true;
  @Input() showButton02: boolean = false;
  @Input() textButton1: string = 'Editar';  
  @Input() textButton2: string = 'Eliminar';          

  constructor() {}
}
