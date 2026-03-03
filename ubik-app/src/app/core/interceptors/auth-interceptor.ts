import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

const PUBLIC_ENDPOINTS = ['/auth/login', '/auth/register'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const platformId = inject(PLATFORM_ID);

  if (!isPlatformBrowser(platformId)) {
    return next(req);
  }

  // ✅ 1) Cloudinary: JAMÁS agregar Authorization
  const isCloudinary =
    req.url.startsWith('https://api.cloudinary.com/') ||
    req.url.includes('api.cloudinary.com/v1_1/');

  if (isCloudinary) {
    return next(req);
  }

  // ✅ 2) Endpoints públicos de tu API
  if (PUBLIC_ENDPOINTS.some(url => req.url.includes(url))) {
    return next(req);
  }

  const token =
    localStorage.getItem('auth_token') ??
    sessionStorage.getItem('auth_token');

  if (!token) return next(req);

  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    })
  );
};
