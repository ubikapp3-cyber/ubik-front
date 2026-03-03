export interface RegisterUserPayload {
  username: string;
  password: string;
  email: string;
  phoneNumber: string;
  anonymous: boolean;
  roleId: string;
  longitude: number;
  latitude: number;
  birthDate: string;
}