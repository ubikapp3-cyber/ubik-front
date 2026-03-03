import { Component, inject, signal, computed, effect } from '@angular/core';
import { Router } from '@angular/router';
import { UsersService } from '../../core/services/user.service';
import { RoomService } from '../../core/services/room.service';
import { MotelService } from '../../core/services/motel.service';
import { Users } from '../../core/models/users.model';
import { Room } from '../../core/models/room.model';
import { Motel } from '../../core/models/motel.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.html',
})
export class Dashboard {

  private userService = inject(UsersService);
  private roomService = inject(RoomService);
  private motelService = inject(MotelService);
  private router = inject(Router);

  currentUser = signal<Users | null>(null);
  userRole = computed(() => this.currentUser()?.roleId ?? null);

  motel = signal<Motel | null>(null);
  rooms = signal<Room[]>([]);

  constructor() {
    this.loadUser();

    effect(() => {
      if (this.userRole() === 3) {
        this.router.navigate(['/']);
      }

      if (this.userRole() === 2) {
        this.loadOwnerData();
      }
    });
  }

  private loadUser() {
    this.userService.loadProfile().subscribe(user => {
      this.currentUser.set(user);
    });
  }

  private loadOwnerData() {
    this.motelService.getProfile().subscribe(motel => {
      this.motel.set(motel);

      this.roomService.getRoomsByMotel(motel.id)
        .subscribe(rooms => this.rooms.set(rooms));
    });
  }

}