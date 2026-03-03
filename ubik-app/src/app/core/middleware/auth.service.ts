import { Injectable, signal, computed, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private isBrowser: boolean;

  private _user = signal<any | null>(null);
  private _token = signal<string | null>(null);

  user = computed(() => this._user());
  token = computed(() => this._token());
  role = computed(() => this._user()?.roleId ?? null);
  isLogged = computed(() => !!this._token());

  isAdmin = computed(() => this.role() === 7392841056473829);
  isOwner = computed(() => this.role() === 3847261094857362);
  isUser  = computed(() => this.role() === 9182736450192837);

  constructor(@Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);

    if (this.isBrowser) {
      const token = localStorage.getItem('auth_token');
      if (token) this._token.set(token);

      const storedUser = localStorage.getItem('user');
      if (storedUser) this._user.set(JSON.parse(storedUser));
    }
  }

  /* ================= TOKEN ================= */

  setToken(token: string, remember: boolean) {
    this._token.set(token);

    if (!this.isBrowser) return;

    if (remember) {
      localStorage.setItem('auth_token', token);
    } else {
      sessionStorage.setItem('auth_token', token);
    }
  }

  /* ================= USER ================= */

  setUser(user: any) {
    this._user.set(user);

    if (this.isBrowser) {
      localStorage.setItem('user', JSON.stringify(user));
    }
  }

  /* ================= LOGOUT ================= */

  logout() {
    this._user.set(null);
    this._token.set(null);

    if (!this.isBrowser) return;

    localStorage.removeItem('auth_token');
    sessionStorage.removeItem('auth_token');
    localStorage.removeItem('user');
  }
}
