import { Component, OnInit } from '@angular/core';
import { Card } from "../../components/card/card";
import { Room } from '../../core/models/room.model';
import { Button01 } from '../../components/button-01/button-01';
import { Button02 } from '../../components/button-02/button-02';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-rooms-motel',
  standalone: true,
  imports: [Card, RouterLink],
  templateUrl: './rooms-motel.html',
})
export class RoomsMotel /*implements OnInit*/ {

  habitaciones: Room[] = [];  

}