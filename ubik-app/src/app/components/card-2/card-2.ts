import { Component, OnInit } from '@angular/core';

interface Card2Info{
  imagen: string;
  titulo: string;
}

@Component({
  selector: 'app-card-2',
  imports: [],
  templateUrl: './card-2.html',
})
export class Card2 implements OnInit {
  card2: Card2Info ={
    imagen: '',
    titulo: ''
  };
  ngOnInit(): void {
    this.cargarDatos();
  }
  cargarDatos(): void{
    this.card2 = {
      imagen: 'https://res.cloudinary.com/du4tcug9q/image/upload/v1763726311/image-habitation_mmy7ly.png',
      titulo: 'Aniversario especial'};
  }
}
