#!/bin/bash
# =============================================================
# Script de arranque automático - Ubik Microservices
# Ubicación: /home/azureuser/ubik-start.sh
# =============================================================

LOG_FILE="/var/log/ubik-startup.log"
COMPOSE_DIR="/home/azureuser/ubik-front/microservicios/microreactivo"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "=========================================="
log "Iniciando despliegue de Ubik..."
log "=========================================="

# -------------------------------------------------------------
# 1. Esperar a que Docker esté listo
# -------------------------------------------------------------
log "Esperando que Docker esté disponible..."
until docker info > /dev/null 2>&1; do
    log "Docker no está listo, esperando 3s..."
    sleep 3
done
log "Docker está listo."

# -------------------------------------------------------------
# 2. Matar procesos que puedan ocupar los puertos
# -------------------------------------------------------------
log "Liberando puertos 8080, 8081, 8083, 8084..."
pkill docker-proxy 2>/dev/null || true
sleep 2

# -------------------------------------------------------------
# 3. Limpiar contenedores y redes anteriores
# -------------------------------------------------------------
log "Limpiando contenedores anteriores..."
cd "$COMPOSE_DIR"

docker compose down --remove-orphans 2>> "$LOG_FILE"
docker network prune -f 2>> "$LOG_FILE"

# Verificar que los puertos estén libres
for PORT in 8080 8081 8083 8084; do
    PID=$(lsof -ti :$PORT 2>/dev/null)
    if [ -n "$PID" ]; then
        log "Puerto $PORT ocupado por PID $PID, matando..."
        kill -9 $PID 2>/dev/null || true
    fi
done

sleep 3

# -------------------------------------------------------------
# 4. Levantar los microservicios
# -------------------------------------------------------------
log "Levantando microservicios con Docker Compose..."
docker compose up -d 2>> "$LOG_FILE"

if [ $? -eq 0 ]; then
    log "Docker Compose levantado correctamente."
else
    log "ERROR: Docker Compose falló. Revisa $LOG_FILE"
    exit 1
fi

# -------------------------------------------------------------
# 5. Esperar a que el gateway esté healthy
# -------------------------------------------------------------
log "Esperando que el gateway esté listo..."
RETRIES=20
COUNT=0

until curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; do
    COUNT=$((COUNT + 1))
    if [ $COUNT -ge $RETRIES ]; then
        log "WARN: Gateway no respondió después de $RETRIES intentos. Continuando de todas formas."
        break
    fi
    log "Gateway no está listo aún, intento $COUNT/$RETRIES..."
    sleep 10
done

if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    log "Gateway está UP y respondiendo."
fi

# -------------------------------------------------------------
# 6. Reiniciar Nginx
# -------------------------------------------------------------
log "Reiniciando Nginx..."
systemctl restart nginx 2>> "$LOG_FILE"

if [ $? -eq 0 ]; then
    log "Nginx reiniciado correctamente."
else
    log "ERROR: Nginx falló al reiniciar."
    exit 1
fi

log "=========================================="
log "Despliegue completado exitosamente."
log "=========================================="
