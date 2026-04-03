# UBIK — Backend Microservices Platform

> Plataforma de gestión y reservas de moteles construida sobre una arquitectura de microservicios reactivos con Java Spring Boot WebFlux.

---

## Tabla de Contenidos

- [Visión General](#visión-general)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Microservicios](#microservicios)
- [Stack Tecnológico](#stack-tecnológico)
- [Arquitectura Hexagonal](#arquitectura-hexagonal)
- [Seguridad y Autenticación](#seguridad-y-autenticación)
- [Observabilidad](#observabilidad)
- [Infraestructura y Despliegue](#infraestructura-y-despliegue)
- [Flujos de Negocio Principales](#flujos-de-negocio-principales)
- [Modelo de Datos](#modelo-de-datos)
- [Variables de Entorno](#variables-de-entorno)
- [Estructura del Repositorio](#estructura-del-repositorio)

---

## Visión General

UBIK es un sistema backend de gestión de establecimientos de hospedaje (moteles) que permite a propietarios registrar y administrar sus establecimientos, a usuarios realizar reservas en línea, y a administradores supervisar y aprobar contenido. El sistema procesa pagos en tiempo real a través de Stripe y notifica a los usuarios por correo electrónico ante eventos relevantes.

**Características principales:**

- API completamente reactiva y no bloqueante (Spring WebFlux + R2DBC)
- Arquitectura hexagonal (Ports & Adapters) en cada microservicio
- Autenticación stateless con JWT, incluyendo soporte para OAuth2 con Google
- Procesamiento de pagos con Stripe (PaymentIntents + Webhooks)
- Generación y envío de facturas PDF adjuntas al correo
- Upload y gestión de imágenes en Cloudinary
- Observabilidad completa: métricas (Prometheus), logs centralizados (Loki), visualización (Grafana) y alertas (Alertmanager)
- Control de acceso basado en roles: `ADMIN`, `PROPERTY_OWNER`, `USER`
- Soporte para eventos en tiempo real vía Server-Sent Events (SSE)

---

## Arquitectura del Sistema

```
                        ┌─────────────────────────────────────┐
                        │           FRONTEND (Vercel)          │
                        └──────────────────┬──────────────────┘
                                           │ HTTPS
                        ┌──────────────────▼──────────────────┐
                        │         API GATEWAY  :8080           │
                        │   JWT Validation · CORS · Routing    │
                        └──┬────────┬────────┬────────┬───────┘
                           │        │        │        │
              ┌────────────▼──┐ ┌───▼───┐ ┌─▼──────┐ ┌▼─────────────┐
              │ UserMgmt :8081│ │Motel  │ │Notif.  │ │Payment :8085 │
              │               │ │:8083  │ │:8084   │ │              │
              └───────┬───────┘ └───┬───┘ └────────┘ └──────┬───────┘
                      │             │                        │
              ┌───────▼─────────────▼────────────────────────▼───────┐
              │                  PostgreSQL (R2DBC)                    │
              └───────────────────────────────────────────────────────┘
                      │             │                        │
              ┌───────▼──┐   ┌──────▼─────┐          ┌──────▼──────┐
              │  Gmail    │   │ Cloudinary │          │   Stripe    │
              │  (SMTP)   │   │  (CDN)     │          │   (Pagos)   │
              └──────────┘   └────────────┘          └─────────────┘
```

Todos los servicios corren sobre una red Docker interna (`ubik-network`) y se comunican directamente por nombre de contenedor sin pasar por el gateway para las llamadas internas.

---

## Microservicios

### 1. `gateway` — Puerto 8080

Punto de entrada único al sistema. Implementa Spring Cloud Gateway sobre WebFlux.

**Responsabilidades:**
- Enrutamiento de requests a los microservicios correspondientes
- Validación de tokens JWT antes de dejar pasar cualquier request autenticado
- Inyección de headers de identidad (`X-User-Username`, `X-User-Role`, `X-User-Id`) en los requests hacia los servicios internos
- Configuración CORS para el frontend desplegado en Vercel
- Logging global de requests y responses con duración

**Rutas configuradas (order matters):**

| Ruta | Destino | Acceso |
|------|---------|--------|
| `/api/admin/**` | motel-management | ADMIN |
| `/api/auth/motels/my-motels` | motel-management | Autenticado |
| `/api/auth/motels/{userId}` | motel-management | Autenticado |
| `/api/auth/**` | usermanagement | Público |
| `/api/user/**` | usermanagement | Autenticado |
| `/api/motels/**` (GET) | motel-management | Público |
| `/api/rooms/**` (GET) | motel-management | Público |
| `/api/services/**` (GET) | motel-management | Público |
| `/api/reservations/**` | motel-management | Autenticado |
| `/api/payments/webhook` | payment-service | Público (Stripe) |
| `/api/payments/**` | payment-service | Autenticado |

El orden de las rutas es explícitamente controlado para que las rutas más específicas tengan prioridad sobre las más genéricas (e.g., `/api/auth/motels/my-motels` antes que `/api/auth/**`).

---

### 2. `usermanagement-service` — Puerto 8081

Gestiona el ciclo de vida de los usuarios: registro, autenticación, perfil y recuperación de contraseña.

**Funcionalidades:**
- Registro con hash BCrypt (strength 12)
- Login con JWT stateless (expiración configurable, default 24h)
- Autenticación social con Google (OAuth2 / `GoogleIdTokenVerifier`)
- Recuperación de contraseña via token UUID con expiración de 1 hora
- Perfil de usuario con datos geográficos opcionales (latitud/longitud) y fecha de nacimiento
- Soft delete (campo `deleted_at`) para desactivación de cuentas sin borrado físico
- Envío de correo de bienvenida en el registro y correo de recuperación

**Endpoints:**

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/register` | Registro de nuevo usuario |
| POST | `/api/auth/login` | Login con username/password |
| POST | `/api/auth/google` | Login/registro con Google IdToken |
| POST | `/api/auth/reset-password-request` | Solicitar reset de contraseña |
| POST | `/api/auth/reset-password` | Aplicar nuevo password con token |
| GET | `/api/user` | Obtener perfil propio |
| GET | `/api/user/{id}` | Obtener perfil por ID |
| PUT | `/api/user` | Actualizar perfil |
| DELETE | `/api/user` | Desactivar cuenta (soft delete) |

---

### 3. `motel-management-service` — Puerto 8083

El microservicio más complejo del sistema. Gestiona la entidad central de negocio: establecimientos, habitaciones, servicios y reservas.

**Dominio:**

- **Motel**: Entidad principal con flujo de aprobación (`PENDING → UNDER_REVIEW → APPROVED / REJECTED`). Incluye datos legales (RUES, RNT, documentos del propietario). Solo los moteles `APPROVED` son visibles públicamente.
- **Room**: Habitaciones asociadas a un motel. Solo se pueden crear si el motel está `APPROVED`. Soporta múltiples imágenes y asociación con servicios.
- **Service**: Catálogo de servicios/amenidades (WiFi, jacuzzi, TV, etc.) asociables a habitaciones mediante tabla pivote.
- **Reservation**: Reservas con máquina de estados (`PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT / CANCELLED`). Incluye detección de solapamiento de fechas, generación de código de confirmación único y notificación por correo.

**Código de confirmación:** Formato `YYMMDD-####-AAA` generado con un contador atómico diario en PostgreSQL (UPSERT con `RETURNING`) más un sufijo UUID aleatorio para garantizar unicidad en alta concurrencia.

**Funcionalidades adicionales:**
- Subida y gestión de imágenes en Cloudinary (con roles: PROFILE, COVER, GALLERY)
- Dashboard para propietarios: ingresos diarios, tasa de ocupación, reservas por estado
- Tablero de estado de habitaciones en tiempo real
- Streaming de reservas vía SSE (`/api/reservations/stream`)
- Sincronización de zona horaria del cliente mediante header `X-Client-Time`

---

### 4. `notification-service` — Puerto 8084

Microservicio de envío de correos electrónicos HTML via SMTP (Gmail).

**Características:**
- Soporte para HTML en los emails (`MimeMessageHelper` con `setText(html, true)`)
- Soporte para adjuntos en base64 (facturas PDF)
- Completamente síncrono (Spring MVC, no WebFlux) para simplicidad
- Health check de Spring Mail deshabilitado para evitar que timeouts SMTP afecten la disponibilidad reportada

**Endpoint:**

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/notifications/email` | Enviar correo con HTML y adjunto opcional |

Los demás microservicios lo llaman directamente por red interna Docker con el header `X-Internal-Request: true`.

---

### 5. `payment-service` — Puerto 8085

Gestiona el ciclo de vida de pagos mediante la integración con Stripe.

**Flujo de pago:**
1. El frontend solicita la creación de un PaymentIntent → el servicio lo crea en Stripe y retorna el `clientSecret`
2. El frontend usa Stripe.js para que el usuario complete el pago directamente contra Stripe (sin que los datos de tarjeta pasen por el backend)
3. Stripe envía un evento webhook al backend al completar o fallar el pago
4. El backend valida la firma del webhook, actualiza el estado del pago en la BD y:
   - Si exitoso: confirma la reserva en motel-management-service y genera + envía la factura PDF al usuario
   - Si fallido: registra el mensaje de error

**Características:**
- Generación de facturas PDF con iTextPDF adjuntas al correo de confirmación
- Todas las llamadas a la SDK de Stripe (bloqueante) se ejecutan en `Schedulers.boundedElastic()` para no bloquear el event loop reactivo
- La publishable key de Stripe se expone en `/api/payments/config` para que el frontend la consuma en tiempo de ejecución

---

## Stack Tecnológico

### Core

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17 | Lenguaje base |
| Spring Boot | 3.2.5 | Framework principal |
| Spring WebFlux | 3.2.5 | Servidor reactivo no bloqueante (Netty) |
| Spring Cloud Gateway | 2023.0.1 | API Gateway reactivo |
| Spring Security (Reactive) | 3.2.5 | Autenticación y autorización |
| Project Reactor | — | Programación reactiva (Mono/Flux) |
| Maven | 3.9.6 | Gestión de dependencias (multi-módulo) |

### Persistencia

| Tecnología | Uso |
|------------|-----|
| PostgreSQL | Base de datos relacional principal |
| Spring Data R2DBC | Acceso reactivo a BD (sin bloqueo) |
| R2DBC Pool | Pool de conexiones reactivo |

### Seguridad

| Tecnología | Uso |
|------------|-----|
| JJWT 0.12.6 | Generación y validación de JWT |
| BCrypt (strength 12) | Hash de contraseñas |
| Google API Client | Verificación de Google ID Tokens |

### Integraciones Externas

| Servicio | Uso |
|---------|-----|
| Stripe | Procesamiento de pagos (PaymentIntents + Webhooks) |
| Cloudinary | Almacenamiento y CDN de imágenes |
| Gmail SMTP | Envío de correos transaccionales |
| iTextPDF | Generación de facturas en PDF |

### Observabilidad

| Herramienta | Puerto | Función |
|-------------|--------|---------|
| Prometheus | 9090 | Recolección y almacenamiento de métricas |
| Grafana | 3000 | Dashboards y visualización |
| Loki | 3100 | Agregación de logs |
| Promtail | — | Agente de recolección de logs Docker |
| Alertmanager | 9093 | Gestión y enrutamiento de alertas |
| cAdvisor | 8086 | Métricas de contenedores Docker |
| Micrometer + Prometheus Registry | — | Exposición de métricas JVM y HTTP |

### Infraestructura

| Tecnología | Uso |
|------------|-----|
| Docker / Docker Compose | Contenedorización y orquestación local/cloud |
| Azure VM | Servidor de producción |
| DuckDNS | DNS dinámico para la VM |
| Vercel | Hosting del frontend |

---

## Arquitectura Hexagonal

Cada microservicio (excepto `notification-service`) implementa **Ports & Adapters** de forma estricta:

```
┌─────────────────────────────────────────────────────────┐
│                      MICROSERVICIO                       │
│                                                         │
│  ┌─────────────┐    ┌───────────────┐    ┌───────────┐  │
│  │  Adaptadores│    │    DOMINIO     │    │Adaptadores│  │
│  │  de Entrada │───▶│               │───▶│de Salida  │  │
│  │             │    │  - Modelos    │    │           │  │
│  │  - REST     │    │  - Servicios  │    │  - R2DBC  │  │
│  │    Controllers│  │  - Puertos IN │    │  - WebClient│ │
│  │  - DTOs     │    │  - Puertos OUT│    │  - Stripe │  │
│  │  - Mappers  │    │               │    │  - Cloudin│  │
│  └─────────────┘    └───────────────┘    └───────────┘  │
└─────────────────────────────────────────────────────────┘
```

**Estructura de paquetes en `motel-management-service`:**

```
com.ubik.motelmanagement
├── domain
│   ├── model/          # Entidades de dominio (Java Records inmutables)
│   ├── port/
│   │   ├── in/         # Puertos de entrada (interfaces de casos de uso)
│   │   └── out/        # Puertos de salida (interfaces de repositorios y servicios externos)
│   └── service/        # Implementaciones de los casos de uso
└── infrastructure
    ├── adapter/
    │   ├── in/
    │   │   └── web/    # Controllers REST, DTOs, Mappers, ExceptionHandler
    │   └── out/
    │       ├── persistence/  # Entidades R2DBC, Repositories, Mappers, Adapters
    │       ├── notification/ # Cliente HTTP al notification-service
    │       └── WebClient/    # Cliente HTTP al user-management-service
    ├── config/         # Configuración de beans (Cloudinary, TransactionalOperator)
    ├── filter/         # WebFilter para captura de tiempo del cliente
    └── service/        # Servicios de infraestructura (Cloudinary, ConfirmationCode, etc.)
```

**Principios clave:**

- El **dominio** no depende de ninguna clase de Spring ni de frameworks de infraestructura
- Los **modelos de dominio** son Java Records inmutables, con métodos de negocio puros (e.g., `motel.approve()`, `reservation.canCheckIn()`)
- Los **puertos** son interfaces puras; el dominio solo conoce las abstracciones, nunca las implementaciones
- Los **adaptadores** implementan los puertos y contienen toda la lógica de infraestructura
- Las **transacciones** reactivas se gestionan mediante `TransactionalOperator` de Spring R2DBC

---

## Seguridad y Autenticación

### Flujo JWT

```
Cliente → POST /api/auth/login
       ← JWT (sub: username, role: roleId, userId: id, exp: +24h)

Cliente → GET /api/reservations (Authorization: Bearer <token>)
       → Gateway: JwtAuthenticationFilter valida firma y extrae claims
       → Inyecta headers: X-User-Username, X-User-Role, X-User-Id
       → Microservicio: lee headers (sin re-validar el JWT)
```

El gateway actúa como **única barrera de autenticación**. Los microservicios internos confían en los headers que el gateway inyecta, lo que evita la duplicación de lógica de validación JWT.

### Roles

Los roles son IDs numéricos configurados por variable de entorno (no strings fijos), lo que permite rotarlos sin cambiar código:

| Rol | Variable | Permisos |
|-----|----------|---------|
| ADMIN | `ROLE_ID_ADMIN` | Acceso total, aprobación/rechazo de moteles |
| PROPERTY_OWNER | `ROLE_ID_PROPERTY_OWNER` | CRUD de sus propios moteles, habitaciones y servicios |
| USER | `ROLE_ID_USER` | Lectura pública, crear/gestionar sus propias reservas y pagos |

### Rutas públicas en el Gateway

- Todo GET de `/api/motels/**`, `/api/rooms/**`, `/api/services/**`
- Todo `/api/auth/**` (excepto `/api/auth/motels/**`)
- `POST /api/payments/webhook` (Stripe no envía JWT)
- `GET /actuator/**`
- Requests `OPTIONS` (CORS preflight)

### SSE (Server-Sent Events)

Para clientes SSE que no pueden enviar headers personalizados, el gateway también acepta el token JWT como query parameter (`?access_token=<token>` o `?token=<token>`).

---

## Observabilidad

### Métricas (Prometheus + Grafana)

Cada microservicio expone `/actuator/prometheus` con métricas de:
- HTTP: tasa de requests, latencia (percentiles p50, p75, p95, p99), SLOs (100ms, 500ms, 1s, 2s), distribución por status code
- JVM: heap usado/máximo, GC pausas, threads activos
- R2DBC: conexiones activas, idle y allocadas del pool

**Alertas configuradas:**

| Alerta | Condición | Severidad |
|--------|-----------|-----------|
| `ServiceDown` | `up == 0` por 1 min | Critical |
| `HighErrorRate` | Tasa 5xx > 5% por 2 min | Critical |
| `SlowResponseTime` | p95 > 2s por 5 min | Warning |
| `JvmHeapCritical` | Heap > 95% por 2 min | Critical |
| `PaymentServiceDown` | Payment service caído por 30s | Critical |
| `PaymentHighErrorRate` | Tasa 5xx en pagos > 2% | Critical |
| `GatewayUnauthorizedSpike` | > 50 respuestas 401/s | Warning |

### Logs (Loki + Promtail)

Los logs de todos los contenedores Docker se recolectan automáticamente mediante Promtail leyendo el socket Docker. En perfil `docker`, todos los servicios emiten logs en formato JSON estructurado (Logstash encoder) que incluyen `traceId`, `spanId`, nombre del servicio y entorno.

El dashboard de Grafana incluye un panel de logs de error en tiempo real que filtra por nivel `ERROR` en todos los contenedores del sistema.

### Dashboard Grafana

Se provisionan automáticamente dos dashboards:
- **Ubik Microservices Overview**: estado de servicios, request rate, latencia p95, tasa de errores 5xx, JVM heap
- **Ubik Microservices Master Console**: uptime, CPU, throughput, métricas JVM detalladas, R2DBC pool, distribución de status HTTP

---

## Infraestructura y Despliegue

### Docker Compose

El sistema usa dos archivos Compose separados que se combinan al desplegar:

- `docker-compose.yml`: Los 5 microservicios de aplicación
- `docker-compose.monitoring.yml`: Stack completo de observabilidad (Prometheus, Grafana, Loki, Promtail, Alertmanager, cAdvisor) + Stripe CLI para webhooks locales

### Script de Despliegue (`deploy-azure.sh`)

Automatiza el ciclo completo de despliegue en la VM Azure:

1. Espera disponibilidad DNS
2. Pull de la rama `respaldo` desde Git
3. Detiene el stack actual limpiamente (`docker compose down --remove-orphans`)
4. Mata procesos Java residuales y libera puertos
5. Limpieza profunda de contenedores y redes Docker
6. Re-crea la red `ubik-network`
7. Configura `ip_forward` e `iptables` para el routing Docker
8. Construye imágenes solo si es necesario (flag `--build` o si hay menos de 5 imágenes)
9. Levanta el stack completo

### Construcción de Imágenes (Multi-stage Docker)

Cada Dockerfile usa build multi-etapa para minimizar el tamaño de la imagen final:

```dockerfile
# Stage 1: Build con Maven (incluye todo el proyecto padre)
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
COPY . .
RUN mvn clean package -pl <module> -am -DskipTests -ntp

# Stage 2: Runtime mínimo
FROM eclipse-temurin:17-jre-alpine
# Usuario no-root, wget para healthcheck
RUN addgroup -S spring && adduser -S spring -G spring
RUN apk add --no-cache wget
USER spring:spring
COPY --from=build /app/<module>/target/*.jar app.jar
HEALTHCHECK --interval=30s CMD wget ... /actuator/health
```

La opción `-am` (also-make) en Maven compila los módulos de los que depende el módulo objetivo, lo que permite compilar un microservicio individual usando el POM padre del proyecto multi-módulo.

### Healthchecks y Dependencias

El gateway espera a que los 4 microservicios estén `healthy` antes de iniciarse (`condition: service_healthy`). Payment service espera a motel-management y notification-service. Esto garantiza que el sistema solo acepta tráfico cuando todos los servicios están listos.

---

## Flujos de Negocio Principales

### Registro y alta de un establecimiento

```
PROPERTY_OWNER registra motel → Estado: PENDING
ADMIN pone en revisión         → Estado: UNDER_REVIEW
ADMIN aprueba (verifica info legal completa: RUES, RNT, documentos) → Estado: APPROVED
PROPERTY_OWNER puede crear habitaciones y asociarles servicios
```

### Flujo de reserva con pago

```
Usuario busca habitaciones disponibles (GET /api/rooms)
Usuario verifica disponibilidad por fechas (GET /api/reservations/room/{id}/available)
Usuario crea reserva → Estado: PENDING, código de confirmación generado
Usuario inicia pago (POST /api/payments/create-intent) → clientSecret retornado
Usuario confirma pago en frontend con Stripe.js
Stripe envía webhook payment_intent.succeeded
  → Payment: SUCCEEDED
  → Reserva: CONFIRMED
  → Email con factura PDF enviado al usuario
```

### Generación de código de confirmación

El código sigue el formato `YYMMDD-####-AAA`:

```sql
-- UPSERT atómico con RETURNING para garantizar unicidad
INSERT INTO reservation_counters (date, counter, ...)
VALUES (:date, 1, ...) 
ON CONFLICT (date) 
DO UPDATE SET counter = reservation_counters.counter + 1
RETURNING counter;
```

El sufijo de 3 caracteres aleatorios (del UUID) actúa como desempate adicional para escenarios de alta concurrencia.

---

## Modelo de Datos

### Entidades principales

```
users
├── id, username, password (bcrypt), email, phone_number
├── role_id, anonymous, registration_time, deleted_at (soft delete)
├── reset_token, reset_token_expiry
└── longitude, latitude, birth_date

motel
├── id, name, address, phone_number, description, city, property_id
├── latitude, longitude, date_created
├── approval_status, approval_date, approved_by_user_id, rejection_reason
└── rues, rnt, owner_document_type, owner_document_number,
    owner_full_name, legal_representative_name, legal_document_url

motel_images
└── id, motel_id, image_url, order_index, role (PROFILE/COVER/GALLERY), created_at

room
└── id, motel_id, number, room_type, price, description, is_available, latitude, longitude

room_images
└── id, room_id, image_url, order_index

service
└── id, name, description, icon, created_at

room_service (tabla pivote N:M)
└── room_id, service_id

reservations
├── id, room_id, user_id, check_in_date, check_out_date
├── status, total_price, special_requests
├── confirmation_code, created_at, updated_at
└── (triggers para manejar ubik.client_time en zona horaria del cliente)

reservation_counters
└── date (PK), counter, created_at, updated_at

payments
├── id, reservation_id, user_id, motel_id
├── stripe_payment_intent_id, amount_cents, currency
└── status, failure_message, created_at, updated_at
```

---

## Variables de Entorno

El sistema usa un único `.env` en la raíz del proyecto, compartido por todos los servicios:

| Variable | Descripción |
|----------|-------------|
| `SPRING_R2DBC_URL` | URL de conexión R2DBC a PostgreSQL |
| `SPRING_R2DBC_USERNAME` | Usuario de la BD |
| `SPRING_R2DBC_PASSWORD` | Contraseña de la BD |
| `JWT_SECRET` | Clave secreta para firmar/validar JWT (mínimo 32 chars) |
| `JWT_EXPIRATION` | Expiración del JWT en milisegundos (default: 86400000 = 24h) |
| `ROLE_ID_ADMIN` | ID numérico del rol administrador |
| `ROLE_ID_PROPERTY_OWNER` | ID numérico del rol propietario |
| `ROLE_ID_USER` | ID numérico del rol usuario estándar |
| `GOOGLE_CLIENT_ID` | Client ID de Google OAuth2 |
| `CLOUDINARY_CLOUD_NAME` | Nombre del cloud en Cloudinary |
| `CLOUDINARY_API_KEY` | API Key de Cloudinary |
| `CLOUDINARY_API_SECRET` | API Secret de Cloudinary |
| `STRIPE_SECRET_KEY` | Clave secreta de Stripe (`sk_live_...` o `sk_test_...`) |
| `STRIPE_PUBLISHABLE_KEY` | Clave publicable de Stripe (`pk_live_...`) |
| `STRIPE_WEBHOOK_SECRET` | Secreto para validar firma de webhooks Stripe |
| `MAIL_USERNAME` | Correo Gmail para envío de notificaciones |
| `MAIL_PASSWORD` | App Password de Gmail |
| `FRONTEND_URL` | URL del frontend para configuración CORS |
| `MOTEL_MANAGEMENT_URL` | URL interna del motel-management-service |
| `GRAFANA_ADMIN_USER` | Usuario admin de Grafana (default: admin) |
| `GRAFANA_ADMIN_PASSWORD` | Contraseña admin de Grafana (default: admin) |

---

## Estructura del Repositorio

```
microreactivo/
├── pom.xml                          # POM padre del proyecto multi-módulo Maven
├── docker-compose.yml               # Stack de microservicios de aplicación
├── docker-compose.monitoring.yml    # Stack de observabilidad
├── deploy-azure.sh                  # Script de despliegue automatizado
│
├── gateway/                         # API Gateway (Spring Cloud Gateway)
│   ├── src/main/java/com/example/gateway/
│   │   ├── application/
│   │   │   ├── config/SecurityConfig.java      # CORS + reglas de autorización
│   │   │   └── filter/JwtAuthenticationFilter.java
│   │   ├── domain/port/out/JwtValidatorPort.java
│   │   ├── filter/
│   │   │   ├── AuthorizationFilter.java        # Gateway filter factory por ruta
│   │   │   └── RequestLoggingFilter.java       # Logging global de requests
│   │   └── infrastructure/adapter/out/jwt/JwtValidatorAdapter.java
│   └── src/main/resources/
│       ├── application.yml          # Rutas del gateway + configuración de Actuator
│       └── logback-spring.xml
│
├── userManagement/                  # Gestión de usuarios y autenticación
│   └── src/main/java/com/ubik/usermanagement/
│       ├── application/
│       │   ├── port/in/             # Interfaces: UserUseCase, UserProfileUseCase
│       │   ├── port/out/            # Interfaces: UserRepositoryPort, JwtPort, etc.
│       │   └── usecase/             # UserService, UserProfileService
│       ├── domain/model/User.java   # Record inmutable con validaciones
│       └── infrastructure/
│           ├── adapter/in/web/      # AuthController, UserProfileController, DTOs
│           └── adapter/out/         # JwtAdapter, UserRepository R2DBC, NotificationAdapter
│
├── motelManegement/                 # Núcleo del negocio (moteles, habitaciones, reservas)
│   └── src/main/java/com/ubik/motelmanagement/
│       ├── domain/
│       │   ├── model/               # Motel, Room, Service, Reservation, etc.
│       │   ├── port/in/             # MotelUseCasePort, RoomUseCasePort, ReservationUseCasePort
│       │   ├── port/out/            # MotelRepositoryPort, RoomRepositoryPort, NotificationPort, etc.
│       │   └── service/             # MotelService, RoomService, ServiceService, ReservationService
│       └── infrastructure/
│           ├── adapter/in/web/      # Controllers, DTOs, Mappers, GlobalExceptionHandler
│           ├── adapter/out/
│           │   ├── persistence/     # Entidades R2DBC, Repositories, Adapters, Mappers
│           │   ├── notification/    # NotificationAdapter (HTTP client)
│           │   └── WebClient/       # UserClientAdapter (HTTP client)
│           ├── config/              # CloudinaryConfig, TxConfig (R2DBC transacciones)
│           ├── filter/ClientTimeFilter.java
│           └── service/             # CloudinaryService, ConfirmationCodeService, MotelImageService
│
├── notificationService/             # Envío de correos electrónicos
│   └── src/main/java/com/ubik/notificationservice/
│       ├── controller/NotificationController.java
│       ├── dto/NotificationRequest.java
│       └── service/NotificationService.java   # JavaMailSender con MimeMessage
│
├── paymentService/                  # Integración con Stripe
│   └── src/main/java/com/ubik/paymentservice/
│       ├── application/service/
│       │   ├── PaymentService.java             # Orquestación del flujo de pago
│       │   └── InvoiceCreator.java             # Generación de PDF con iTextPDF
│       ├── domain/
│       │   ├── model/Payment.java, PaymentStatus.java
│       │   └── port/in/out/                   # PaymentUseCasePort, StripePort, etc.
│       └── infrastructure/adapter/
│           ├── in/web/PaymentController.java
│           └── out/
│               ├── stripe/StripeAdapter.java   # SDK Stripe (boundedElastic scheduler)
│               ├── persistence/               # PaymentEntity, R2DBC Repository
│               ├── motelmanagement/           # ReservationConfirmationAdapter
│               ├── user/UserInfoAdapter.java
│               └── notification/NotificationAdapter.java
│
└── monitoring/
    ├── prometheus/
    │   ├── prometheus.yml                     # Targets de scraping por servicio
    │   └── rules/
    │       ├── microservices.yml              # Alertas de disponibilidad y errores
    │       └── jvm.yml                        # Alertas de JVM y contenedores
    ├── grafana/
    │   ├── provisioning/
    │   │   ├── datasources/Datasources.yml    # Prometheus + Loki auto-provisionados
    │   │   └── dashboards/Dashboards.yml
    │   ├── dashboards/overview.json           # Dashboard de overview multi-servicio
    │   └── fixed_dashboard.json              # Dashboard detallado por instancia
    ├── loki/loki-config.yml                   # Retención 30 días, índice TSDB v13
    ├── promtail/promptail-config.yml          # Recolección desde Docker socket
    └── alertmanager/alertmanager.yml          # Enrutamiento a email por severidad/servicio
```

---

## Consideraciones de Diseño Notables

**Programación reactiva end-to-end**: Desde el gateway hasta los repositorios, todo el stack utiliza tipos reactivos (`Mono`/`Flux`). Las únicas excepciones son la SDK de Stripe y Cloudinary (bloqueantes), que se aíslan en `Schedulers.boundedElastic()` para no bloquear el event loop de Netty.

**Zona horaria del cliente**: El sistema maneja fechas de reserva en la zona horaria local del usuario (Colombia, UTC-5) mediante el header `X-Client-Time` que el frontend envía con cada request. El `ClientTimeFilter` propaga este valor a través del contexto reactivo (`contextWrite`) para que los servicios de dominio y las queries SQL usen la hora local correcta.

**Transacciones reactivas**: En lugar de `@Transactional` (solo funciona con hilos bloqueantes), el sistema usa `TransactionalOperator.transactional(Mono)` de Spring R2DBC para envolver las operaciones que requieren atomicidad.

**Separación de concerns en imágenes**: Las imágenes de moteles tienen tres roles (PROFILE, COVER, GALLERY) con lógica de ordenamiento diferente. Las imágenes de galería tienen `order_index` incremental; PROFILE y COVER no tienen orden. La query de carga las ordena explícitamente con un `CASE WHEN` en SQL.

**Comunicación interna directa**: Los servicios se llaman entre sí directamente por nombre de contenedor Docker (sin pasar por el gateway), lo que evita la validación JWT redundante en llamadas internas y reduce la latencia.
