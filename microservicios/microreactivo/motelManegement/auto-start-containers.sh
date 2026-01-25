#!/bin/bash

# =================================================================
# Script de auto-inicio de contenedores Docker
# Se ejecuta automáticamente al iniciar la VM
# =================================================================

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

# Archivo de log
LOG_FILE="/var/log/docker-autostart.log"

# Función para logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "=========================================="
log "Iniciando contenedores Docker..."
log "=========================================="

# Esperar a que Docker esté listo
MAX_WAIT=60
WAIT_COUNT=0

while ! docker info >/dev/null 2>&1; do
    if [ $WAIT_COUNT -ge $MAX_WAIT ]; then
        log "ERROR: Docker no inició después de ${MAX_WAIT}s"
        exit 1
    fi
    
    log "Esperando a que Docker esté listo... ($WAIT_COUNT/${MAX_WAIT}s)"
    sleep 1
    WAIT_COUNT=$((WAIT_COUNT + 1))
done

log "✅ Docker está listo"

# Iniciar contenedores usando docker-compose
cd /home/user/Ubik-App/microservicios/microreactivo || {
    log "ERROR: No se pudo acceder al directorio del proyecto"
    exit 1
}

# Opción 1: Iniciar solo la base de datos
log "Iniciando base de datos PostgreSQL..."
docker-compose up -d postgres adminer 2>&1 | tee -a "$LOG_FILE"

# Esperar a que PostgreSQL esté listo
log "Esperando a que PostgreSQL esté listo..."
MAX_ATTEMPTS=30
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    if docker exec motel-postgres-db pg_isready -U postgres -d motel_management_db >/dev/null 2>&1; then
        log "✅ PostgreSQL está listo"
        break
    fi
    
    log "Esperando PostgreSQL... intento $ATTEMPT/$MAX_ATTEMPTS"
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

if [ $ATTEMPT -gt $MAX_ATTEMPTS ]; then
    log "⚠️  PostgreSQL tardó mucho en iniciarse"
fi

# Opción 2: Si también quieres iniciar el sistema completo (descomenta):
# log "Iniciando sistema completo..."
# docker-compose -f docker-compose-full.yml up -d 2>&1 | tee -a "$LOG_FILE"

# Mostrar estado de contenedores
log "Estado de contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | tee -a "$LOG_FILE"

log "=========================================="
log "✅ Contenedores iniciados correctamente"
log "=========================================="

# Mostrar información útil
echo ""
echo -e "${GREEN}🐘 PostgreSQL disponible en:${NC}"
echo "   Host: localhost:5432"
echo "   Base de datos: motel_management_db"
echo "   Usuario: postgres"
echo "   Password: carlosmanuel"
echo ""
echo -e "${BLUE}🌐 Adminer disponible en:${NC}"
echo "   http://localhost:8081"
echo ""
echo -e "${YELLOW}📝 Ver logs:${NC}"
echo "   tail -f $LOG_FILE"
echo ""
