import { Component, computed, signal, inject} from '@angular/core';
import { filter } from 'rxjs';
import { Logo01 } from "../../components/logo-01/logo-01";
import { Button01 } from "../../components/button-01/button-01";
import { Router, RouterLink, NavigationEnd } from '@angular/router';


import { AuthService } from '../../core/middleware/auth.service';
import { routes } from '../../app.routes';  
import { RoomsOfferts } from '../../views/rooms-offerts/rooms-offerts';

const ROUTES = {
  HOME: '/',
  EXPLORE: '/explore',
  LOGIN: '/login',
  REGISTER: '/select-register',
  PROFILE: '/userProfile', 
  OWNER: '/listProperty', 
  ADMIN: '/' ,
  OFFERTS: '/'
};

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [Logo01, Button01],
  templateUrl: './header.html',
})
export class Header {
  title = 'ENCUENTRA EL LUGAR PERFECTO PARA TU MOMENTO ESPECIAL';
  subtitle = 'Descubre moteles y espacios únicos cerca de ti, de forma rápida y segura.';

  currentUrl = signal<string>('/');

  showSearch = computed(() => {
    return this.currentUrl() === '/';
  });

  // constructor(private router: Router) {
  //   this.currentUrl.set(this.normalizeUrl(this.router.url));

  //   this.router.events
  //     .pipe(filter(e => e instanceof NavigationEnd))
  //     .subscribe((e: any) => {
  //       this.currentUrl.set(this.normalizeUrl(e.urlAfterRedirects));
  //     });
  // }

  constructor(private router: Router) {
    // URL inicial limpia (sin #hash)
    this.currentUrl.set(this.router.url.split('#')[0] || '/');

    // Escuchar cambios de ruta
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.currentUrl.set(this.router.url.split('#')[0] || '/');
      });
  }

  // private normalizeUrl(url: string): string {
  //   let clean = url.split('?')[0].replace(/\/$/, '');
  //   clean = clean.split('#')[0];
  //   return clean === '' ? '/' : clean;
  // }

  // ===== Inject =====
  private auth = inject(AuthService);

  ROUTES = ROUTES;

  // ===== Logo =====
  logo: string =
    'https://res.cloudinary.com/du4tcug9q/image/upload/v1763473322/Logo_Ubik_jxgzqi.png';

  // ===== Signals del Auth =====
  isLogged = this.auth.isLogged;
  role = this.auth.role;

  // ===== Navegación =====
  AppRoutes = routes;

  navigateTo(route: string) {
    this.router.navigate([route]);
  }

  // ===== Logout =====
  logout() {
    this.auth.logout();
    this.router.navigate([ROUTES.HOME]);
  }
}

