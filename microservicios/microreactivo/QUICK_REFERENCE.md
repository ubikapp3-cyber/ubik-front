# Quick Reference - Deployment Commands

## Initial Setup

```bash
# 1. Navigate to project directory
cd microservicios/microreactivo

# 2. Create .env file from template
cp .env.example .env

# 3. Edit .env and set your passwords and JWT secret
nano .env  # or use your preferred editor

# 4. Build all images
docker compose build --no-cache

# 5. Start all services
docker compose up -d

# 6. Wait for services to be ready (30-60 seconds)
docker compose ps

# 7. Check health
curl http://localhost:8080/actuator/health
```

## Daily Operations

```bash
# View all services status
docker compose ps

# View logs (all services)
docker compose logs -f

# View logs (specific service)
docker compose logs -f gateway
docker compose logs -f usermanagement
docker compose logs -f motel-management
docker compose logs -f postgres

# Restart a service
docker compose restart gateway

# Stop all services
docker compose down

# Stop and remove data (WARNING: deletes database)
docker compose down -v
```

## After Code Changes

```bash
# Rebuild specific service
docker compose build --no-cache gateway
docker compose up -d gateway

# Rebuild all services
docker compose down
docker compose build --no-cache
docker compose up -d
```

## Troubleshooting

```bash
# Check if services can reach each other
docker compose exec gateway ping usermanagement
docker compose exec gateway ping motel-management
docker compose exec gateway ping postgres

# Access PostgreSQL directly
docker compose exec postgres psql -U postgres -d ubik_db

# Check environment variables
docker compose exec gateway env | grep SPRING
docker compose exec usermanagement env | grep DB_

# Inspect network
docker network inspect microreactivo_ubik-network
```

## Testing Endpoints

```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Gateway routes info
curl http://localhost:8080/actuator/gateway/routes

# UserManagement health
curl http://localhost:8081/actuator/health

# Motel Management health  
curl http://localhost:8083/actuator/health

# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }'
```

## Port Reference

- **8080**: Gateway (main API entry point)
- **8081**: User Management
- **8083**: Motel Management  
- **5432**: PostgreSQL

## Service Names

For configuration and internal communication:
- `postgres` - Database
- `usermanagement` - User Management Service
- `motel-management` - Motel Management Service
- `api-gateway` - API Gateway (container name)

## Container Names

- `postgres-db`
- `usermanagement`
- `motel-management`
- `api-gateway`

## Environment Variables Required

In `.env` file:
```
JWT_SECRET=<32+ characters>
JWT_EXPIRATION=86400000
POSTGRES_DB=ubik_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<your-password>

# Standard Spring Boot R2DBC variables (used by motel-management)
SPRING_R2DBC_URL=r2dbc:postgresql://postgres:5432/ubik_db
SPRING_R2DBC_USERNAME=postgres
SPRING_R2DBC_PASSWORD=<your-password>

# Legacy R2DBC variables (used by userManagement for compatibility)
DB_R2DBC_URL=r2dbc:postgresql://postgres:5432/ubik_db
DB_USERNAME=postgres
DB_PASSWORD=<your-password>
```

**Note**: Both SPRING_* and DB_* variables are required because userManagement uses a different naming convention. This maintains backward compatibility until the service can be refactored.

## Common Issues & Fixes

### Services won't start
```bash
# Check logs
docker compose logs -f

# Rebuild
docker compose down
docker compose build --no-cache
docker compose up -d
```

### Database connection failed
```bash
# Verify postgres is healthy
docker compose ps postgres

# Restart dependent services
docker compose restart usermanagement motel-management
```

### Gateway returns 503
```bash
# Check if backend services are running
docker compose ps

# Rebuild gateway
docker compose build --no-cache gateway
docker compose up -d gateway
```

### Port already in use
```bash
# Find what's using the port
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process or change port in docker compose.yml
```
