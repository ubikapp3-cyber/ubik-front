# Ubik App - Sistema de Gestión de Moteles

Sistema completo de gestión de moteles con arquitectura de microservicios y frontend Angular.

## 🚀 Estructura del Proyecto

```
ubik-front/
├── frontend/                          # Aplicación Angular 20 + TailwindCSS
├── microservicios/
│   └── microreactivo/                # Microservicios Spring WebFlux
│       ├── gateway/                  # API Gateway (puerto 8080)
│       ├── userManagement/           # Autenticación y usuarios (puerto 8081)
│       ├── products/                 # Gestión de productos (puerto 8082)
│       └── motelManegement/          # Gestión de moteles (puerto 8083)
└── FRONTEND_INTEGRATION_GUIDE.md     # 📘 Guía de integración completa
```

## 📚 Documentación

### Para Desarrolladores Frontend

**👉 [FRONTEND_INTEGRATION_GUIDE.md](./FRONTEND_INTEGRATION_GUIDE.md)** - Guía completa de integración

Esta guía contiene:
- ✅ Arquitectura del sistema
- ✅ Configuración del entorno de desarrollo
- ✅ Documentación completa de todos los endpoints API
- ✅ Estructuras de datos (DTOs) con interfaces TypeScript
- ✅ Autenticación JWT y autorización
- ✅ Ejemplos de código Angular (servicios, interceptors, componentes)
- ✅ Ejemplos con Fetch API (JavaScript vanilla)
- ✅ Manejo de errores y troubleshooting
- ✅ Mejores prácticas de seguridad

### Para Desarrolladores Backend

- **[microservicios/microreactivo/README.md](./microservicios/microreactivo/README.md)** - Instrucciones para ejecutar microservicios
- **[microservicios/microreactivo/DOCKER_BUILD_GUIDE.md](./microservicios/microreactivo/DOCKER_BUILD_GUIDE.md)** - Guía de construcción con Docker
- **[microservicios/microreactivo/TESTING_MOTEL_GATEWAY.md](./microservicios/microreactivo/TESTING_MOTEL_GATEWAY.md)** - Pruebas del Gateway

## ⚡ Quick Start

### Frontend (Angular)

```bash
cd frontend
npm install
npm start
# Abre http://localhost:4200
```

### Backend (Microservicios)

```bash
cd microservicios/microreactivo

# Compilar todos los módulos
mvn clean install -DskipTests

# Iniciar servicios (en terminales separadas)
mvn -pl gateway spring-boot:run          # Puerto 8080
mvn -pl userManagement spring-boot:run   # Puerto 8081
mvn -pl products spring-boot:run         # Puerto 8082
mvn -pl motelManegement spring-boot:run  # Puerto 8083
```

### Requisitos Previos

- **Java 17**
- **Maven 3.9+**
- **Node.js 18+** y npm
- **PostgreSQL 12+** (para userManagement y motelManagement)
- **MySQL 8+** (para products)

## 🔧 Configuración

### Variables de Entorno

```bash
# JWT
JWT_SECRET=tu_clave_secreta_jwt
JWT_EXPIRATION=86400000

# PostgreSQL - UserManagement
DB_R2DBC_URL=r2dbc:postgresql://localhost:5432/user_management_db
DB_USERNAME=postgres
DB_PASSWORD=tu_password

# PostgreSQL - MotelManagement
SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/motel_management_db
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=tu_password
```

## 📡 API Endpoints

Todos los endpoints están disponibles a través del **API Gateway** en `http://localhost:8080/api`

### Principales Endpoints

| Endpoint                | Método | Descripción                       | Auth |
|-------------------------|--------|-----------------------------------|------|
| `/api/auth/register`    | POST   | Registrar nuevo usuario           | No   |
| `/api/auth/login`       | POST   | Iniciar sesión (obtener JWT)      | No   |
| `/api/user`             | GET    | Obtener perfil de usuario         | Sí   |
| `/api/motels`           | GET    | Listar moteles                    | No   |
| `/api/rooms`            | GET    | Listar habitaciones               | Sí   |
| `/api/services`         | GET    | Listar servicios                  | Sí   |
| `/api/reservations`     | GET    | Listar reservas                   | Sí   |
| `/api/products`         | GET    | Listar productos                  | Sí   |

Ver **[FRONTEND_INTEGRATION_GUIDE.md](./FRONTEND_INTEGRATION_GUIDE.md)** para documentación completa de todos los endpoints.

## 🏗️ Arquitectura

```
┌─────────────────┐
│  Frontend       │
│  Angular 20     │
│  Port: 4200     │
└────────┬────────┘
         │
         │ HTTP
         ▼
┌─────────────────┐
│  API Gateway    │
│  Port: 8080     │
└────────┬────────┘
         │
         ├──────────────┬──────────────┬──────────────┐
         │              │              │              │
         ▼              ▼              ▼              ▼
┌───────────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐
│ UserManagement│ │ Products │ │  Motel   │ │   Future     │
│   Port: 8081  │ │Port: 8082│ │Port: 8083│ │  Services    │
│  PostgreSQL   │ │  MySQL   │ │PostgreSQL│ │              │
└───────────────┘ └──────────┘ └──────────┘ └──────────────┘
```

## 🛡️ Seguridad

- **JWT (JSON Web Tokens)** para autenticación
- **BCrypt** para hash de contraseñas (strength: 12)
- **CORS** configurado en el Gateway
- **Spring Security** en microservicios
- **Validación de entrada** con Jakarta Validation

## 🧪 Testing

```bash
# Backend
cd microservicios/microreactivo
mvn test

# Frontend
cd frontend
npm test
```

## 📦 Despliegue con Docker

```bash
cd microservicios/microreactivo

# Gateway
docker build -f gateway/Dockerfile -t ubik/gateway:latest .

# UserManagement
docker build -f userManagement/Dockerfile -t ubik/user-management:latest .

# MotelManagement
docker build -f motelManegement/Dockerfile -t ubik/motel-management:latest .
```

Ver **[DOCKER_BUILD_GUIDE.md](./microservicios/microreactivo/DOCKER_BUILD_GUIDE.md)** para más detalles.

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 📝 Licencia

Este proyecto es privado y confidencial.

## 📧 Contacto

- **Equipo Ubik App**
- **Repositorio**: [ubikapp3-cyber/ubik-front](https://github.com/ubikapp3-cyber/ubik-front)

---

**Última actualización**: Diciembre 2024
