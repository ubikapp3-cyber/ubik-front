import { Component, Input } from '@angular/core';
import { Room } from '../../core/models/room.model';
import { Button01 } from "../button-01/button-01";
import { Button02 } from "../button-02/button-02";

@Component({
  selector: 'app-card',
  standalone: true,
  templateUrl: './card.html',
  imports: [Button01, Button02]
})
export class Card {

  @Input() card!: Room;

  showDescription = true;

}