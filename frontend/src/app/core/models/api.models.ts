/**
 * API Models and DTOs (Data Transfer Objects)
 * These interfaces match the backend API contracts
 * Follows Interface Segregation Principle (ISP) - focused, minimal interfaces
 */

/**
 * Login request DTO
 * Matches backend /api/auth/login endpoint
 */
export interface LoginRequestDto {
  email: string;
  password: string;
}

/**
 * Login response DTO
 * Matches backend /api/auth/login response
 */
export interface LoginResponseDto {
  token: string;
  userId: string;
  email: string;
  role: string;
  expiresAt?: string;
}

/**
 * Register request DTO for client registration
 * Matches backend /api/auth/register endpoint
 */
export interface RegisterClientRequestDto {
  fullName: string;
  email: string;
  password: string;
  birthDate: string; // ISO date format YYYY-MM-DD
}

/**
 * Register request DTO for establishment registration
 * Matches backend /api/auth/register-establishment endpoint
 */
export interface RegisterEstablishmentRequestDto {
  ownerName: string;
  ownerEmail: string;
  identificationNumber: string;
  establishmentName: string;
  establishmentEmail: string;
  rues: string;
  rnt: string;
  password: string;
  country: string;
  department: string;
  municipality: string;
  acceptedTerms: boolean;
}

/**
 * Register response DTO
 * Matches backend registration response
 */
export interface RegisterResponseDto {
  userId: string;
  email: string;
  message: string;
}

/**
 * Password reset request DTO
 * Matches backend /api/auth/reset-password-request endpoint
 */
export interface PasswordResetRequestDto {
  email: string;
}

/**
 * Password reset DTO
 * Matches backend /api/auth/reset-password endpoint
 */
export interface PasswordResetDto {
  token: string;
  newPassword: string;
}

/**
 * API error response DTO
 * Standard error format from backend
 */
export interface ApiErrorResponseDto {
  error: string;
  message: string;
  status: number;
  timestamp: string;
  path?: string;
}

/**
 * User profile DTO
 * Matches backend /api/user endpoint
 */
export interface UserProfileDto {
  id: string;
  email: string;
  fullName: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Update user profile DTO
 * Matches backend PUT /api/user endpoint
 */
export interface UpdateUserProfileDto {
  fullName?: string;
  email?: string;
}
