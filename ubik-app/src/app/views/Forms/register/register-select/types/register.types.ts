// Tipos base
export type RegistrationType = 'establecimiento' | 'cliente' | null;

// ----------------------
// ESTADO DEL WIZARD
// ----------------------
export interface RegisterWizardState {
  type: RegistrationType;
  currentStep: number;
}

// ----------------------
// CLIENTE
// ----------------------
export interface ClientRegisterData {
  fullName?: string;
  email?: string;
  password?: string;
  birthDate?: {
    day?: string;
    month?: string;
    year?: string;
  };
}

// ----------------------
// OWNER (DUEÑO)
// ----------------------
export interface OwnerData {
  fullName?: string;
  email?: string;
  documentId?: string;
  documentFront?: File;
  documentBack?: File;
}

// ----------------------
// INFO ESTABLECIMIENTO
// ----------------------
export interface EstablishmentInfo {
  name?: string;
  email?: string;
  rues?: string;
  rnt?: string;
  country?: string;
  department?: string;
  city?: string;
  password?: string;
}

// ----------------------
// MEDIA
// ----------------------
export interface EstablishmentMedia {
  gallery?: File[];
}

// ----------------------
// DATA GLOBAL REGISTRO
// ----------------------
export interface RegisterDataState {
  client: ClientRegisterData;
  establishment: {
    owner: OwnerData;
    info: EstablishmentInfo;
    media: EstablishmentMedia;
  };
}

