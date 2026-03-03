/*import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Button01 } from '../../components/button-01/button-01';
import { CardOffers, CardOff } from '../../components/card-offers/card-offers';
import { CardRoom, CardHabitacion } from '../../components/card-room/card-room';

import { MotelService } from '../../core/services/motel.service';
import { Motel } from '../../core/models/motel.model';

@Component({
  selector: 'app-perfile-motel',
  standalone: true,
  templateUrl: './motel-profile.html',
  imports: [CommonModule, Button01, CardOffers, CardRoom],
})
export class MotelProfile implements OnInit {

  private motelService = inject(MotelService);

  ofertas: CardOff[] = [];
  CardHab: CardHabitacion[] = [];

  profile: any;

  ngOnInit() {
    this.motelService.getProfile().subscribe({
      next: (motel: Motel) => {

        this.profile = {
          imageBack: 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?w=1600',
          imagePerfile: motel.imagesUrls?.[0] ?? 'https://via.placeholder.com/150',
          nombre: motel.name,
          ubicacion: motel.city,
          description: motel.description,
          address: motel.address,
        };

        this.CardHab = motel.rooms?.map(room => ({
          id: room.id,
          nombre: room.room_type,
          number: room.num_or_name,
          tipo: room.room_type,
          servicios: room.services?.flatMap(rs =>
            rs.service_id.map(service => service.name)
          ) ?? [],
          descripcion: room.description,
          imagen: room.photos?.[0]?.url ?? './assets/images/ubikLogo.jpg',
          price: room.price,
        })) ?? [];
      }
    });
  }
}*/