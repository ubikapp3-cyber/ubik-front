/**
 * Environment Configuration Service
 * Follows Single Responsibility Principle (SRP) - manages environment configuration only
 * Provides centralized configuration for API URLs and application settings
 */

import { Injectable } from '@angular/core';

/**
 * Environment configuration interface
 */
export interface EnvironmentConfig {
  production: boolean;
  apiBaseUrl: string;
  authApiUrl: string;
  motelApiUrl: string;
}

/**
 * Default development configuration
 */
const developmentConfig: EnvironmentConfig = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api',
  authApiUrl: 'http://localhost:8081/api',
  motelApiUrl: 'http://localhost:8082/api',
};

/**
 * Production configuration
 * TODO: Update with actual production URLs
 */
const productionConfig: EnvironmentConfig = {
  production: true,
  apiBaseUrl: '/api',
  authApiUrl: '/api/auth',
  motelApiUrl: '/api/motel',
};

@Injectable({
  providedIn: 'root',
})
export class EnvironmentService {
  private readonly config: EnvironmentConfig;

  constructor() {
    // Use production config if in production mode, otherwise use development
    // This can be determined by checking window.location or build configuration
    const isProduction = this.isProductionEnvironment();
    this.config = isProduction ? productionConfig : developmentConfig;
  }

  /**
   * Get the current environment configuration
   * @returns Environment configuration
   */
  getConfig(): EnvironmentConfig {
    return { ...this.config };
  }

  /**
   * Get API base URL for general API calls
   * @returns API base URL
   */
  getApiBaseUrl(): string {
    return this.config.apiBaseUrl;
  }

  /**
   * Get authentication API URL
   * @returns Authentication API URL
   */
  getAuthApiUrl(): string {
    return this.config.authApiUrl;
  }

  /**
   * Get motel management API URL
   * @returns Motel API URL
   */
  getMotelApiUrl(): string {
    return this.config.motelApiUrl;
  }

  /**
   * Check if running in production mode
   * Uses early return pattern
   * @returns true if production, false otherwise
   */
  isProduction(): boolean {
    return this.config.production;
  }

  /**
   * Determine if environment is production
   * Uses early returns for clarity
   * @returns true if production environment detected
   */
  private isProductionEnvironment(): boolean {
    // Check if window is available (SSR compatibility)
    if (typeof window === 'undefined') {
      return false;
    }

    // Check hostname for production indicators
    const hostname = window.location.hostname;
    
    // Early return for localhost
    if (hostname === 'localhost' || hostname === '127.0.0.1') {
      return false;
    }

    // Consider anything else as production
    return true;
  }
}
