/**
 * Type definitions for the login view
 * Follows Interface Segregation Principle (ISP)
 */

/**
 * Login form data structure
 * Must match backend LoginRequest
 */
export interface LoginFormData {
  username: string;
  password: string;
}

/**
 * Validation error structure
 */
export interface ValidationError {
  field: string;
  message: string;
}

/**
 * Authentication result from login service
 */
export interface AuthResult {
  success: boolean;
  message: string;
  token?: string;
  userId?: string;
  redirectUrl?: string;
}

/**
 * OAuth provider types
 */
export enum OAuthProvider {
  GOOGLE = 'GOOGLE',
  FACEBOOK = 'FACEBOOK',
}

/**
 * Login state management
 */
export interface LoginState {
  formData: Partial<LoginFormData>;
  errors: ValidationError[];
  isSubmitting: boolean;
  rememberMe: boolean;
}
