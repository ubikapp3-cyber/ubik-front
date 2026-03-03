export interface ForgotPasswordRequest {
  email: string;
}

export interface VerifyTokenRequest {
  token: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}