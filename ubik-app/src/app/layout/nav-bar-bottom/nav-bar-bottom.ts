import { Component, OnInit, OnDestroy, Inject, PLATFORM_ID, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { Subscription } from 'rxjs';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
// 1. IMPORTAR NUEVOS ÍCONOS
import {
  faHouse,
  faCompass,
  faCircleUser,
  faRightFromBracket,
  faBars,
  faBuilding,
  faEye,
  IconDefinition,
} from '@fortawesome/free-solid-svg-icons';

import { AuthService } from '../../core/middleware/auth.service';

const ROUTES = {
  HOME: '/',
  EXPLORE: '/explore',
  LOGIN: '/login',
  REGISTER: '/select-register',
  PROPERTY: '/listProperty',
  PROFILE: '/userProfile',
  OWNER: '/listProperty',
  ADMIN: '/',
};

@Component({
  selector: 'app-nav-bar-bottom',
  templateUrl: './nav-bar-bottom.html',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule],
})
export class NavBarBottomComponent implements OnInit, OnDestroy {
  auth = inject(AuthService);

  isLogged = this.auth.isLogged;
  role = this.auth.role;

  menuOpen = false;

  readonly icons: Record<
    'house' | 'compass' | 'user' | 'logout' | 'menu' | 'owner' | 'admin',
    IconDefinition
  > = {
    house: faHouse,
    compass: faCompass,
    user: faCircleUser,
    logout: faRightFromBracket,
    menu: faBars,
    owner: faBuilding,
    admin: faEye,
  };

  private routerSubscription?: Subscription;

  constructor(
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object,
  ) {}

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.routerSubscription = this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.menuOpen = false;
      });
  }

  ngOnDestroy(): void {
    this.routerSubscription?.unsubscribe();
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  navigateTo(path: string): void {
    this.closeMenu();
    this.router.navigate([path]);
  }

  logout(): void {
    this.auth.logout();
    this.navigateTo(ROUTES.LOGIN);
  }

  public readonly AppRoutes = ROUTES;
}
