# Backend Integration Guide

This document describes how the login and register functionality has been integrated with the backend API.

## Architecture Overview

The integration follows a layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────┐
│          Components (UI Layer)              │
│  LoginComponent, RegisterComponent          │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│     Business Logic Services                 │
│  LoginService, RegisterService              │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│          API Services                       │
│  AuthApiService, RegistrationApiService     │
└────────────────┬────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────┐
│     Infrastructure Layer                    │
│  HttpClientService, Auth Interceptor        │
└─────────────────────────────────────────────┘
```

## Key Components

### 1. Environment Configuration

**File:** `src/app/core/config/environment.config.ts`

Manages environment-specific configuration including API URLs:

```typescript
- apiBaseUrl: 'http://localhost:8080/api'     // Gateway
- authApiUrl: 'http://localhost:8081/api'     // Auth Service
- motelApiUrl: 'http://localhost:8082/api'    // Motel Service
```

**SOLID Principles:**
- **SRP**: Only manages environment configuration
- **OCP**: Easy to extend with new configuration values

### 2. HTTP Client Service

**File:** `src/app/core/services/http-client.service.ts`

Centralized HTTP communication with:
- Automatic URL building based on endpoint
- Consistent error handling
- Type-safe requests
- Transformation of backend errors to user-friendly messages

**SOLID Principles:**
- **SRP**: Only handles HTTP communication
- **DIP**: Depends on Angular's HttpClient abstraction
- **OCP**: Extensible for new HTTP methods

**Features:**
- Early returns for error handling
- Automatic base URL selection
- User-friendly error messages in Spanish

### 3. Auth Interceptor

**File:** `src/app/core/interceptors/auth.interceptor.ts`

Automatically injects JWT tokens into requests:
- Skips public endpoints (login, register)
- Adds `Authorization: Bearer <token>` header
- SSR-compatible (checks for localStorage availability)

**SOLID Principles:**
- **SRP**: Only handles JWT token injection
- Uses early returns for clarity

### 4. API Models

**File:** `src/app/core/models/api.models.ts`

TypeScript interfaces matching backend contracts:
- `LoginRequestDto` / `LoginResponseDto`
- `RegisterClientRequestDto` / `RegisterEstablishmentRequestDto`
- `RegisterResponseDto`
- `ApiErrorResponseDto`

**SOLID Principles:**
- **ISP**: Each interface is minimal and focused

### 5. Auth API Service

**File:** `src/app/views/Forms/login/services/auth-api.service.ts`

Handles authentication API calls:
- `login(credentials)` → POST /api/auth/login
- `registerClient(data)` → POST /api/auth/register
- `registerEstablishment(data)` → POST /api/auth/register-establishment
- `requestPasswordReset(email)` → POST /api/auth/reset-password-request
- `resetPassword(data)` → POST /api/auth/reset-password

**SOLID Principles:**
- **SRP**: Only handles auth API communication
- **DIP**: Depends on HttpClientService abstraction

### 6. Login Service (Updated)

**File:** `src/app/views/Forms/login/services/login.service.ts`

**Changes:**
- ✅ Now uses `AuthApiService` instead of mock data
- ✅ Transforms API responses to internal `AuthResult` format
- ✅ Proper error handling with user-friendly messages
- ✅ Maintains validation logic and token storage

**Example:**
```typescript
login(data: LoginFormData): Observable<AuthResult> {
  // Validate
  const errors = this.validateForm(data);
  if (errors.length > 0) {
    return throwError(() => ({ success: false, message: '...' }));
  }

  // Call real backend
  return this.authApiService.login({
    email: data.email,
    password: data.password
  }).pipe(
    map(response => ({
      success: true,
      token: response.token,
      userId: response.userId,
      redirectUrl: '/home'
    })),
    catchError(error => throwError(() => ({
      success: false,
      message: error.message
    })))
  );
}
```

### 7. Registration API Service

**File:** `src/app/views/Forms/register/services/registration-api.service.ts`

Handles registration API calls:
- `registerClient(data)` → POST /api/auth/register
- `registerEstablishment(data)` → POST /api/auth/register-establishment

**SOLID Principles:**
- **SRP**: Only handles registration API communication
- **DIP**: Delegates to AuthApiService

### 8. Register Service (Updated)

**File:** `src/app/views/Forms/register/services/register.service.ts`

**Changes:**
- ✅ Now uses `RegistrationApiService` instead of mock data
- ✅ Formats birth date to ISO format (YYYY-MM-DD)
- ✅ Transforms form data to API DTOs
- ✅ Proper error handling
- ✅ Maintains all validation logic

**Example:**
```typescript
submitClientRegistration(data: ClientFormData): Observable<RegistrationResult> {
  // Validate
  const errors = this.validateClientForm(data);
  if (errors.length > 0) {
    return throwError(() => ({ success: false, ... }));
  }

  // Format and call backend
  const birthDate = this.formatBirthDate(data.birthDay, data.birthMonth, data.birthYear);
  return this.registrationApiService.registerClient({
    fullName: data.fullName,
    email: data.email,
    password: data.password,
    birthDate: birthDate
  }).pipe(
    map(response => ({ success: true, userId: response.userId, ... })),
    catchError(error => throwError(() => ({ success: false, ... })))
  );
}
```

## API Endpoints

### User Management Service (localhost:8081)

#### Authentication
- **POST** `/api/auth/login`
  - Request: `{ email, password }`
  - Response: `{ token, userId, email, role }`

- **POST** `/api/auth/register`
  - Request: `{ fullName, email, password, birthDate }`
  - Response: `{ userId, email, message }`

- **POST** `/api/auth/register-establishment`
  - Request: `{ ownerName, ownerEmail, identificationNumber, establishmentName, ... }`
  - Response: `{ userId, email, message }`

- **POST** `/api/auth/reset-password-request`
  - Request: `{ email }`
  - Response: `{ message }`

- **POST** `/api/auth/reset-password`
  - Request: `{ token, newPassword }`
  - Response: `{ message }`

## SOLID Principles Applied

### Single Responsibility Principle (SRP)
- ✅ Each service has one clear purpose
- ✅ Environment config only manages configuration
- ✅ HTTP client only handles HTTP communication
- ✅ Auth interceptor only handles JWT injection
- ✅ API services only handle API calls
- ✅ Business services only handle business logic

### Open/Closed Principle (OCP)
- ✅ Easy to add new API endpoints
- ✅ Easy to add new environment configurations
- ✅ Easy to extend error handling

### Liskov Substitution Principle (LSP)
- ✅ Services can be substituted with mock implementations for testing
- ✅ Observable pattern allows easy testing

### Interface Segregation Principle (ISP)
- ✅ Each DTO interface is minimal and focused
- ✅ No interface contains unnecessary properties

### Dependency Inversion Principle (DIP)
- ✅ Services depend on abstractions (Observable, HttpClient)
- ✅ Business logic separated from infrastructure
- ✅ Easy to swap implementations

## Early Returns Pattern

All functions use early returns to reduce nesting:

```typescript
// Instead of:
if (condition1) {
  if (condition2) {
    // Deep logic
  }
}

// We use:
if (!condition1) return;
if (!condition2) return;
// Logic at top level
```

**Examples:**
- Validation checks return early on failure
- Error handling returns immediately
- Guard clauses prevent deep nesting

## Type Safety

All code uses strict TypeScript types:
- ✅ `tsconfig.json` has `strict: true`
- ✅ Explicit interfaces for all data structures
- ✅ No `any` types (except where necessary for events)
- ✅ Explicit return types on all functions
- ✅ Type-safe HTTP requests

## Error Handling

### Backend Errors → User-Friendly Messages

The `HttpClientService` transforms HTTP errors:

```typescript
Status 0   → "No se pudo conectar al servidor"
Status 400 → "Solicitud inválida"
Status 401 → "No autorizado. Por favor, inicie sesión"
Status 403 → "Acceso denegado"
Status 404 → "Recurso no encontrado"
Status 500 → "Error interno del servidor"
Status 503 → "Servicio no disponible"
```

Custom backend messages are preserved when available.

## Testing the Integration

### Prerequisites
1. Start the User Management Service: `mvn -pl userManagement -am spring-boot:run`
2. Ensure PostgreSQL is running with the `userManagement_db` database
3. Start the frontend: `npm run start`

### Test Login
1. Navigate to `/login`
2. Enter credentials:
   - Email: `test@example.com`
   - Password: `password123`
3. Click "Ingresar"
4. Should receive JWT token and redirect to `/home`

### Test Registration
1. Navigate to `/register`
2. Choose "Cliente" or "Establecimiento"
3. Fill in the form
4. Submit
5. Should create user and show success message

## Security Considerations

### Token Storage
- Currently using `localStorage` for JWT tokens
- **Production**: Consider using httpOnly cookies
- Auth interceptor handles token injection automatically

### CORS
- Backend must allow frontend origin
- Development: `http://localhost:4200`
- Production: Configure appropriate origins

### HTTPS
- **Production**: Always use HTTPS
- Protects credentials and tokens in transit

## Future Enhancements

1. **OAuth Integration**: Implement Google/Facebook login flows
2. **Token Refresh**: Implement automatic token refresh
3. **File Upload**: Add multipart/form-data support for establishment images
4. **Retry Logic**: Add automatic retry for failed requests
5. **Request Caching**: Cache GET requests where appropriate
6. **Offline Support**: Add service worker for offline functionality

## Troubleshooting

### Cannot connect to backend
- Check if User Management Service is running on port 8081
- Check CORS configuration in backend
- Check browser console for detailed errors

### 401 Unauthorized errors
- Token may be expired
- Check token storage in localStorage
- Try logging in again

### Validation errors
- Check request body matches backend expectations
- Verify date formats (YYYY-MM-DD for birth dates)
- Check required fields

## Migration Checklist

If updating from mock to production:

- [x] Add `provideHttpClient()` to `app.config.ts`
- [x] Create environment configuration
- [x] Create API models matching backend
- [x] Create HTTP client service
- [x] Create auth interceptor
- [x] Update LoginService
- [x] Update RegisterService
- [x] Test all endpoints
- [ ] Update production URLs
- [ ] Configure CORS
- [ ] Enable HTTPS
- [ ] Add monitoring/logging
