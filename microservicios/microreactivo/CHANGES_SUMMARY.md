# Summary of Changes - Database and Gateway Configuration Fix

## Problems Addressed

### 1. ✅ Database Configuration (CRITICAL)
**Problem**: Services failed with "Failed to obtain R2DBC Connection" because no PostgreSQL database was configured.

**Solution**:
- Added PostgreSQL service to `docker compose.yml`
- Created `.env.example` with all required environment variables
- Added health checks to ensure database is ready before services start
- Configured proper service dependencies

### 2. ✅ Gateway Service Name Inconsistency
**Problem**: Gateway configuration used inconsistent service names:
- Line 33: `usermanagement-service:8081` ❌
- Line 51: `motel-management-service:8083` ❌

**Solution**:
- Updated all routes to use correct service names from docker compose.yml:
  - `usermanagement:8081` ✅
  - `motel-management:8083` ✅

### 3. ✅ Actuator Configuration
**Problem**: Actuator health endpoint needed verification.

**Solution**: 
- Confirmed actuator is properly configured in gateway (already present)
- Added actuator configuration to docker profiles for consistency

## Files Modified

### 1. `docker compose.yml`
**Changes**:
- Added PostgreSQL service with health checks
- Fixed container names to match service references
- Added proper service dependencies with health conditions
- Added volumes for PostgreSQL data persistence
- Updated environment variables for userManagement

**Key additions**:
```yaml
postgres:
  image: postgres:15-alpine
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
```

### 2. `gateway/src/main/resources/application.yml`
**Changes**:
- Fixed userManagement profile route: `usermanagement-service` → `usermanagement`
- Fixed motel-management motels route: `motel-management-service` → `motel-management`

### 3. `.env.example` (NEW)
**Purpose**: Template for environment configuration

**Contents**:
- JWT configuration (secret, expiration)
- PostgreSQL database settings
- R2DBC connection strings
- Alternative variable names for userManagement compatibility

### 4. `gateway/src/main/resources/application-docker.yml` (NEW)
**Purpose**: Docker-specific configuration for gateway

**Features**:
- Consistent service name references
- Complete gateway route configuration
- Actuator endpoints enabled
- Debug logging for troubleshooting

### 5. `userManagement/src/main/resources/application-docker.yml` (NEW)
**Purpose**: Docker-specific configuration for userManagement

**Features**:
- R2DBC connection pool settings
- Database initialization settings
- Health endpoint configuration
- Logging configuration

### 6. `DOCKER_SETUP.md` (NEW)
**Purpose**: Comprehensive deployment guide

**Sections**:
- Quick start instructions
- Service ports and descriptions
- API endpoints documentation
- Troubleshooting guide
- Security notes

## Environment Variables Reference

### Required Variables (.env file)

```bash
# JWT
JWT_SECRET=<minimum 32 characters>
JWT_EXPIRATION=86400000

# PostgreSQL
POSTGRES_DB=ubik_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<your-password>

# R2DBC for motel-management
SPRING_R2DBC_URL=r2dbc:postgresql://postgres:5432/ubik_db
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=<your-password>

# R2DBC for userManagement (different names for compatibility)
# Note: userManagement uses different variable names for historical reasons
# Both sets are required until userManagement is updated to use SPRING_* names
DB_R2DBC_URL=r2dbc:postgresql://postgres:5432/ubik_db
DB_USERNAME=postgres
DB_PASSWORD=<your-password>
```

### Why Two Sets of Database Variables?

The project uses two naming conventions for database configuration:

- **SPRING_R2DBC_*** - Used by motel-management (standard Spring Boot naming)
- **DB_*** - Used by userManagement (legacy naming convention)

Both are included in `.env` to maintain compatibility with existing service configurations. In a future refactoring, userManagement should be updated to use the standard SPRING_R2DBC_* variables.

## Service Architecture

```
┌─────────────┐
│  PostgreSQL │ (postgres:5432)
└──────┬──────┘
       │ (health check)
       ├──────────────────┬──────────────────┐
       │                  │                  │
┌──────▼──────┐   ┌──────▼──────┐   ┌──────▼──────┐
│    User     │   │    Motel    │   │   Gateway   │
│ Management  │   │ Management  │   │             │
│   :8081     │   │   :8083     │   │   :8080     │
└─────────────┘   └─────────────┘   └─────────────┘
```

## Service Names in Docker Network

All services communicate using these hostnames:
- `postgres` - Database server
- `usermanagement` - User management service
- `motel-management` - Motel management service
- `api-gateway` - Gateway (container name, but routes use service names)

## Deployment Instructions

### First Time Setup

1. **Create .env file**:
   ```bash
   cd microservicios/microreactivo
   cp .env.example .env
   # Edit .env with your values
   ```

2. **Build services**:
   ```bash
   docker compose build --no-cache
   ```

3. **Start services**:
   ```bash
   docker compose up -d
   ```

4. **Check logs**:
   ```bash
   docker compose logs -f
   ```

5. **Verify health**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### After Configuration Changes

When modifying application.yml files:

```bash
# Rebuild affected service
docker compose build --no-cache gateway
docker compose up -d gateway
```

### Database Management

```bash
# Access PostgreSQL CLI
docker exec -it postgres-db psql -U postgres -d ubik_db

# View database logs
docker compose logs -f postgres

# Backup database
docker exec postgres-db pg_dump -U postgres ubik_db > backup.sql
```

## Testing the Fix

### 1. Test Database Connection

```bash
# Check if PostgreSQL is healthy
docker compose ps postgres

# Should show: Up (healthy)
```

### 2. Test Gateway Routing

```bash
# Through gateway
curl http://localhost:8080/actuator/health

# Direct to userManagement
curl http://localhost:8081/actuator/health

# Direct to motel-management
curl http://localhost:8083/actuator/health
```

### 3. Test Service Communication

```bash
# Register a user via gateway
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

## Known Limitations

1. **Database Initialization**: Services use `sql.init.mode: always/never`. Consider using Flyway or Liquibase for production.

2. **No SSL/TLS**: Database connections are not encrypted. Add SSL configuration for production.

3. **Single Database**: All services share one database. Consider separate databases per service for better isolation.

4. **Default Passwords**: Change all default passwords before deploying to production.

## Next Steps

1. **Add Database Migration Tool**: Implement Flyway or Liquibase for schema management
2. **Add Products Service**: If needed, add the products microservice
3. **Configure SSL**: Add SSL certificates for secure communication
4. **Add Monitoring**: Implement Prometheus + Grafana for monitoring
5. **Add Logging**: Implement ELK stack or similar for centralized logging
6. **Production Hardening**: Review and update security configurations

## Rollback Procedure

If issues occur:

```bash
# Stop all services
docker compose down

# Remove volumes (WARNING: deletes data)
docker compose down -v

# Return to previous configuration
git checkout main
```

## Support

For issues:
1. Check logs: `docker compose logs -f [service]`
2. Review DOCKER_SETUP.md troubleshooting section
3. Verify .env configuration
4. Check service health endpoints
