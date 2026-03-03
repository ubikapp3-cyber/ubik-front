import { Component, inject, OnInit } from '@angular/core';
import { UsersService } from '../../../core/services/user.service';
import { Users } from '../../../core/models/users.model';
import { Button01 } from "../../../components/button-01/button-01";
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [ CommonModule, Button01 ],
  templateUrl: './user-profile.html',
})
export class UserProfile implements OnInit {
  private usersService = inject(UsersService);

  profile$ = this.usersService.profile$;
  loading = true;

  ngOnInit() {
    this.usersService.loadProfile().subscribe({
      next: () => (this.loading = false),
      error: () => (this.loading = false),
    });
  }

}
