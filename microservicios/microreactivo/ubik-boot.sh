#!/bin/bash
# =================================================================
# UBIK — Script de arranque rápido (usado por systemd en cada boot)
# NO hace git pull ni docker build — solo levanta lo que ya existe.
# Para actualizar: usa deploy-azure.sh manualmente.
# =================================================================

set -e

PROJECT_PATH="$HOME/ubik-front/microservicios/microreactivo"
COMPOSE_FILES="-f docker-compose.yml -f docker-compose.monitoring.yml"
LOG_FILE="/var/log/ubik-boot.log"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "=== Iniciando Ubik Boot ==="

cd "$PROJECT_PATH" || { log "ERROR: No se encontró $PROJECT_PATH"; exit 1; }

# Configurar red interna (puede no existir tras un reboot)
log "Configurando red ubik-network..."
docker network create ubik-network 2>/dev/null && log "Red creada." || log "Red ya existe, OK."

# Habilitar IP forwarding (se resetea en cada boot)
log "Configurando kernel networking..."
sysctl -w net.ipv4.ip_forward=1 > /dev/null
iptables -P FORWARD ACCEPT

# Limpiar contenedores huérfanos del boot anterior
log "Limpiando contenedores anteriores..."
docker compose $COMPOSE_FILES down --remove-orphans 2>/dev/null || true

# Levantar todos los contenedores en background
log "Levantando stack completo..."
docker compose $COMPOSE_FILES up -d

log "=== Boot completado. Gateway en :8080 | Grafana en :3000 ==="
