/**
 * Authentication Interceptor
 * Follows Single Responsibility Principle (SRP) - handles JWT token injection only
 * Automatically adds JWT token to outgoing requests
 */

import { HttpInterceptorFn } from '@angular/common/http';

/**
 * Auth interceptor function
 * Uses early returns to avoid unnecessary processing
 * Injects JWT token into Authorization header for authenticated requests
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Early return if request is to a public endpoint
  if (isPublicEndpoint(req.url)) {
    return next(req);
  }

  // Get token from localStorage
  const token = getAuthToken();

  // Early return if no token available
  if (!token) {
    return next(req);
  }

  // Clone request and add Authorization header
  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authReq);
};

/**
 * Check if endpoint is public (doesn't require authentication)
 * Uses early returns for clarity
 * @param url - Request URL
 * @returns true if public endpoint
 */
function isPublicEndpoint(url: string): boolean {
  // List of public endpoints that don't require authentication
  const publicEndpoints = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/auth/register-establishment',
    '/api/auth/reset-password-request',
    '/api/auth/reset-password',
  ];

  // Check if URL matches any public endpoint
  return publicEndpoints.some((endpoint) => url.includes(endpoint));
}

/**
 * Get authentication token from storage
 * Uses early return pattern
 * @returns JWT token or null
 */
function getAuthToken(): string | null {
  // Early return if localStorage not available (SSR)
  if (typeof localStorage === 'undefined') {
    return null;
  }

  return localStorage.getItem('auth_token');
}
