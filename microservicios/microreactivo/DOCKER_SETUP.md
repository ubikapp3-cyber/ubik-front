# Setup Guide - Docker Deployment

This guide will help you deploy the microservices using Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose V2
- 4GB+ RAM available for Docker

## Quick Start

### 1. Configure Environment Variables

Copy the example environment file and configure it:

```bash
cd microservicios/microreactivo
cp .env.example .env
```

Edit `.env` and set your values:
- `JWT_SECRET`: Set a strong secret key (minimum 32 characters)
- `POSTGRES_PASSWORD`: Set a secure password for PostgreSQL

### 2. Build and Start Services

```bash
# Build all images (first time or after code changes)
docker compose build --no-cache

# Start all services
docker compose up -d

# View logs
docker compose logs -f
```

### 3. Verify Services

Wait for all services to start (approximately 30-60 seconds). Check health:

```bash
# Check all services status
docker compose ps

# Check Gateway health
curl http://localhost:8080/actuator/health

# Check UserManagement health
curl http://localhost:8081/actuator/health

# Check Motel Management health
curl http://localhost:8083/actuator/health
```

## Services and Ports

| Service | Port | Container Name | Description |
|---------|------|----------------|-------------|
| PostgreSQL | 5432 | postgres-db | Database server |
| User Management | 8081 | usermanagement | Authentication & user management |
| Gateway | 8080 | api-gateway | API Gateway (main entry point) |
| Motel Management | 8083 | motel-management | Motel, rooms & services management |

## API Endpoints (via Gateway)

All API requests should go through the Gateway at `http://localhost:8080`

### Authentication Endpoints
```bash
# Register new user
POST http://localhost:8080/api/auth/register

# Login
POST http://localhost:8080/api/auth/login
```

### Motel Management Endpoints
```bash
# Motels
GET    http://localhost:8080/api/motels
POST   http://localhost:8080/api/motels
GET    http://localhost:8080/api/motels/{id}
PUT    http://localhost:8080/api/motels/{id}
DELETE http://localhost:8080/api/motels/{id}

# Rooms
GET    http://localhost:8080/api/rooms
POST   http://localhost:8080/api/rooms
GET    http://localhost:8080/api/rooms/{id}
PUT    http://localhost:8080/api/rooms/{id}
DELETE http://localhost:8080/api/rooms/{id}

# Services
GET    http://localhost:8080/api/services
POST   http://localhost:8080/api/services
GET    http://localhost:8080/api/services/{id}
PUT    http://localhost:8080/api/services/{id}
DELETE http://localhost:8080/api/services/{id}
```

## Troubleshooting

### Services Not Starting

1. **Check logs for specific service:**
   ```bash
   docker compose logs -f [service-name]
   # Example: docker compose logs -f usermanagement
   ```

2. **Verify PostgreSQL is healthy:**
   ```bash
   docker compose ps postgres
   # Should show "healthy" status
   ```

3. **Check environment variables:**
   ```bash
   docker compose config
   ```

### Database Connection Issues

If services fail with "Failed to obtain R2DBC Connection":

1. **Verify PostgreSQL is running:**
   ```bash
   docker compose ps postgres
   ```

2. **Check database connectivity:**
   ```bash
   docker exec -it postgres-db psql -U postgres -d ubik_db
   ```

3. **Restart services:**
   ```bash
   docker compose restart usermanagement motel-management
   ```

### Gateway Routing Issues

If Gateway returns 503 Service Unavailable:

1. **Verify service names match docker compose:**
   - usermanagement (not usermanagement-service)
   - motel-management (not motel-management-service)

2. **Check internal network:**
   ```bash
   docker network inspect microreactivo_ubik-network
   ```

3. **Rebuild Gateway after config changes:**
   ```bash
   docker compose build --no-cache gateway
   docker compose up -d gateway
   ```

### Port Already in Use

If you get "port already allocated" error:

```bash
# Find process using the port (example for 8080)
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Stop the process or change port in docker compose.yml
```

## Rebuilding After Code Changes

```bash
# Rebuild specific service
docker compose build --no-cache [service-name]
docker compose up -d [service-name]

# Rebuild all services
docker compose down
docker compose build --no-cache
docker compose up -d
```

## Stopping and Cleaning Up

```bash
# Stop all services
docker compose down

# Stop and remove volumes (WARNING: deletes database data)
docker compose down -v

# Remove all images
docker compose down --rmi all
```

## Configuration Details

### PostgreSQL Configuration

- **Image**: postgres:15-alpine
- **Database**: ubik_db
- **Default user**: postgres
- **Health check**: Every 10 seconds
- **Persistent storage**: Docker volume `postgres-data`

### Service Dependencies

All services wait for PostgreSQL to be healthy before starting:

```
postgres (healthy) → usermanagement → gateway
                  → motel-management
```

### Spring Profiles

All services use the `docker` profile when running in Docker:
- `application-docker.yml` overrides `application.yml`
- Environment variables are injected via docker compose

## Security Notes

⚠️ **Important**: 
- Never commit `.env` file to version control
- Change default passwords in production
- Use strong JWT secret keys (32+ characters)
- In production, use secrets management (Docker Secrets, Kubernetes Secrets, etc.)

## Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [R2DBC Documentation](https://r2dbc.io/)
