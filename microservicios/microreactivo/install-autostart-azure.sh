#!/bin/bash

# =================================================================
# Script de instalación de auto-inicio para Azure VM
# Configura systemd para iniciar contenedores al arrancar la VM
# SIN PostgreSQL local (usa Azure Flexible Server)
# =================================================================

set -e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}Configurando auto-inicio de contenedores Docker${NC}"
echo -e "${BLUE}Para Azure VM + PostgreSQL Flexible Server${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Verificar que se ejecute como root
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}❌ Este script debe ejecutarse como root o con sudo${NC}"
    echo "Usa: sudo ./install-autostart-azure.sh"
    exit 1
fi

# Obtener el usuario real (no root)
REAL_USER="${SUDO_USER:-$USER}"
USER_HOME=$(eval echo ~$REAL_USER)

# Detectar automáticamente la ruta del proyecto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

echo -e "${YELLOW}📂 Directorio del proyecto: $PROJECT_DIR${NC}"
echo -e "${YELLOW}👤 Usuario: $REAL_USER${NC}"
echo ""

# Verificar que el directorio del proyecto existe
if [ ! -d "$PROJECT_DIR" ]; then
    echo -e "${RED}❌ No se encontró el directorio del proyecto${NC}"
    echo "   Buscado en: $PROJECT_DIR"
    exit 1
fi

# Verificar que existe docker-compose.yml
if [ ! -f "$PROJECT_DIR/docker-compose.yml" ]; then
    echo -e "${RED}❌ No se encontró docker-compose.yml en el proyecto${NC}"
    echo "   Buscado en: $PROJECT_DIR/docker-compose.yml"
    exit 1
fi

# 1. Crear el script de auto-inicio
echo -e "${GREEN}📝 Creando script de auto-inicio...${NC}"

cat > /usr/local/bin/docker-autostart.sh << 'SCRIPT_CONTENT'
#!/bin/bash

# Script de auto-inicio de contenedores Docker para Azure
# SIN PostgreSQL local (usa Azure Flexible Server)

# Archivo de log
LOG_FILE="/var/log/docker-autostart.log"

# Función para logging
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

log "=========================================="
log "🚀 Iniciando contenedores Docker en Azure VM"
log "=========================================="

# Esperar a que Docker esté listo
MAX_WAIT=60
WAIT_COUNT=0

while ! docker info >/dev/null 2>&1; do
    if [ $WAIT_COUNT -ge $MAX_WAIT ]; then
        log "❌ ERROR: Docker no inició después de ${MAX_WAIT}s"
        exit 1
    fi

    log "⏳ Esperando a que Docker esté listo... ($WAIT_COUNT/${MAX_WAIT}s)"
    sleep 1
    WAIT_COUNT=$((WAIT_COUNT + 1))
done

log "✅ Docker está listo"

# Cambiar al directorio del proyecto
cd PROJECT_DIR_PLACEHOLDER || {
    log "❌ ERROR: No se pudo acceder al directorio del proyecto"
    exit 1
}

log "📂 Directorio de trabajo: $(pwd)"

# Verificar que existe .env
if [ ! -f .env ]; then
    log "⚠️  ADVERTENCIA: No se encontró archivo .env"
    log "    Se usarán las variables por defecto"
else
    log "✅ Archivo .env encontrado"
fi

# Limpiar contenedores anteriores
log "🧹 Limpiando contenedores anteriores..."
docker-compose down --remove-orphans 2>&1 | tee -a "$LOG_FILE" || true

# Pequeña pausa para que Docker libere recursos
sleep 3

# Iniciar los servicios
log "🚀 Iniciando microservicios..."
if ! docker-compose up -d 2>&1 | tee -a "$LOG_FILE"; then
    log "❌ Error al iniciar servicios en primer intento"
    log "🔄 Intentando limpieza y reinicio..."

    # Limpieza más profunda
    docker-compose down --remove-orphans -v 2>&1 | tee -a "$LOG_FILE" || true
    sleep 5

    # Reconstruir si es necesario
    log "🔨 Reconstruyendo imágenes..."
    if ! docker-compose build --no-cache 2>&1 | tee -a "$LOG_FILE"; then
        log "❌ Error al reconstruir imágenes"
        exit 1
    fi

    # Segundo intento
    log "🚀 Segundo intento de inicio..."
    if ! docker-compose up -d 2>&1 | tee -a "$LOG_FILE"; then
        log "❌ Error al iniciar servicios después de reconstruir"
        exit 1
    fi
fi

log "✅ Contenedores iniciados"

# Esperar a que los microservicios estén listos
log "⏳ Esperando a que los microservicios se inicialicen..."
sleep 20

# Verificar health de los servicios
check_health() {
    local service_name=$1
    local port=$2
    local max_attempts=15
    local attempt=1

    log "🔍 Verificando $service_name (puerto $port)..."

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f http://localhost:$port/actuator/health >/dev/null 2>&1; then
            log "✅ $service_name está listo y respondiendo"
            return 0
        fi

        if [ $attempt -eq $max_attempts ]; then
            log "⚠️  $service_name no respondió después de $max_attempts intentos"
            log "    Puede estar aún iniciando. Verifica con: docker logs $service_name"
            return 1
        fi

        log "   Intento $attempt/$max_attempts..."
        sleep 4
        attempt=$((attempt + 1))
    done
}

# Verificar servicios principales
check_health "Gateway" "8080"
check_health "User-Management" "8081"
check_health "Motel-Management" "8083"
check_health "Notification-Service" "8084"

# Mostrar estado de contenedores
log ""
log "📊 Estado actual de contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}" | tee -a "$LOG_FILE"

log ""
log "=========================================="
log "✅ Sistema iniciado correctamente"
log "=========================================="
log ""
log "🌐 Servicios disponibles:"
log "   • Gateway:        http://localhost:8080"
log "   • User Mgmt:      http://localhost:8081"
log "   • Motel Mgmt:     http://localhost:8083"
log "   • Notifications:  http://localhost:8084"
log ""
log "📊 Base de datos: Azure PostgreSQL Flexible Server (externo)"
log "   • Host: dbubik.postgres.database.azure.com"
log "   • Puerto: 5432"
log "   • Base de datos: postgres"
log ""
log "💡 Para ver logs: docker-compose logs -f"
log "💡 Para health check: curl http://localhost:8080/actuator/health"
log ""
SCRIPT_CONTENT

# Reemplazar el placeholder con la ruta real
sed -i "s|PROJECT_DIR_PLACEHOLDER|$PROJECT_DIR|g" /usr/local/bin/docker-autostart.sh

# Dar permisos de ejecución
chmod +x /usr/local/bin/docker-autostart.sh

echo -e "${GREEN}✅ Script creado en /usr/local/bin/docker-autostart.sh${NC}"

# 2. Crear el servicio systemd
echo ""
echo -e "${GREEN}📝 Creando servicio systemd...${NC}"

cat > /etc/systemd/system/docker-autostart.service << EOF
[Unit]
Description=Auto-inicio de contenedores Docker - Ubik Microservices
Documentation=https://github.com/tu-repo/ubik-microservices
After=docker.service network-online.target
Requires=docker.service
Wants=network-online.target

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/local/bin/docker-autostart.sh
User=root
WorkingDirectory=$PROJECT_DIR
StandardOutput=journal
StandardError=journal
TimeoutStartSec=600
TimeoutStopSec=120

# Política de reinicio
Restart=on-failure
RestartSec=15
StartLimitInterval=200
StartLimitBurst=3

[Install]
WantedBy=multi-user.target
EOF

echo -e "${GREEN}✅ Servicio creado en /etc/systemd/system/docker-autostart.service${NC}"

# 3. Habilitar el servicio
echo ""
echo -e "${GREEN}⚙️  Habilitando servicio...${NC}"

systemctl daemon-reload
systemctl enable docker-autostart.service

echo -e "${GREEN}✅ Servicio habilitado para auto-inicio${NC}"

# 4. Crear archivo de log
touch /var/log/docker-autostart.log
chmod 666 /var/log/docker-autostart.log

# 5. Crear script de verificación para el usuario
cat > $USER_HOME/check-services.sh << 'EOF'
#!/bin/bash

# Script de verificación de servicios Docker

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   UBIK - Verificación de Servicios        ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

# Estado del servicio systemd
echo -e "${YELLOW}📋 Servicio de auto-inicio:${NC}"
sudo systemctl status docker-autostart --no-pager --lines=5
echo ""

# Contenedores corriendo
echo -e "${YELLOW}🐳 Contenedores activos:${NC}"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | head -n 10
echo ""

# Health checks
echo -e "${YELLOW}🏥 Health Checks:${NC}"

check_service() {
    local name=$1
    local port=$2

    if curl -s -f http://localhost:$port/actuator/health >/dev/null 2>&1; then
        echo -e "  ${GREEN}✅ $name${NC} (puerto $port)"
    else
        echo -e "  ${RED}❌ $name${NC} (puerto $port) - No responde"
    fi
}

check_service "Gateway        " "8080"
check_service "User Management" "8081"
check_service "Motel Mgmt     " "8083"
check_service "Notifications  " "8084"

# Test de conectividad a Azure PostgreSQL
echo ""
echo -e "${YELLOW}🗄️  Base de Datos (Azure):${NC}"
if timeout 3 bash -c "cat < /dev/null > /dev/tcp/dbubik.postgres.database.azure.com/5432" 2>/dev/null; then
    echo -e "  ${GREEN}✅ PostgreSQL Flexible Server${NC} (dbubik.postgres.database.azure.com:5432)"
else
    echo -e "  ${RED}❌ PostgreSQL Flexible Server${NC} - No alcanzable"
    echo -e "     ${YELLOW}Verifica las reglas de firewall en Azure Portal${NC}"
fi

echo ""
echo -e "${YELLOW}📝 Últimas 15 líneas del log:${NC}"
sudo tail -15 /var/log/docker-autostart.log
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   Comandos Útiles                          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""
echo "  sudo systemctl status docker-autostart   # Estado del servicio"
echo "  sudo journalctl -u docker-autostart -f  # Logs en tiempo real"
echo "  docker-compose logs -f                   # Logs de contenedores"
echo "  docker ps                                # Contenedores activos"
echo "  docker stats                             # Uso de recursos"
echo ""
EOF

chmod +x $USER_HOME/check-services.sh
chown $REAL_USER:$REAL_USER $USER_HOME/check-services.sh

# 6. Crear script de limpieza de emergencia
cat > $USER_HOME/emergency-restart.sh << 'EOF'
#!/bin/bash

# Script de reinicio de emergencia

echo "🚨 Reinicio de emergencia de servicios Docker"
echo "=============================================="
echo ""

cd ~/microreactivo || exit 1

echo "🛑 Deteniendo todos los servicios..."
docker-compose down --remove-orphans

echo "🧹 Limpiando contenedores huérfanos..."
docker container prune -f

echo "🔨 Reconstruyendo imágenes..."
docker-compose build --no-cache

echo "🚀 Iniciando servicios..."
docker-compose up -d

echo ""
echo "✅ Proceso completado"
echo "⏳ Espera 30 segundos para que los servicios inicien"
echo ""
echo "Verifica el estado con: ./check-services.sh"
EOF

chmod +x $USER_HOME/emergency-restart.sh
chown $REAL_USER:$REAL_USER $USER_HOME/emergency-restart.sh

echo ""
echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   ✅ Instalación Completada                ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}📋 Archivos creados:${NC}"
echo ""
echo "  ✅ /usr/local/bin/docker-autostart.sh"
echo "  ✅ /etc/systemd/system/docker-autostart.service"
echo "  ✅ $USER_HOME/check-services.sh"
echo "  ✅ $USER_HOME/emergency-restart.sh"
echo "  ✅ /var/log/docker-autostart.log"
echo ""
echo -e "${YELLOW}🔧 Comandos disponibles:${NC}"
echo ""
echo "  # Verificar estado de servicios:"
echo "  ./check-services.sh"
echo ""
echo "  # Iniciar manualmente:"
echo "  sudo systemctl start docker-autostart"
echo ""
echo "  # Ver logs del servicio:"
echo "  sudo journalctl -u docker-autostart -f"
echo ""
echo "  # Ver logs del script:"
echo "  sudo tail -f /var/log/docker-autostart.log"
echo ""
echo "  # Reinicio de emergencia:"
echo "  ./emergency-restart.sh"
echo ""
echo -e "${GREEN}🔄 Los contenedores se iniciarán automáticamente al reiniciar la VM${NC}"
echo ""
echo -e "${YELLOW}💡 Para probar ahora sin reiniciar:${NC}"
echo "   sudo systemctl start docker-autostart"
echo ""
echo -e "${YELLOW}📊 Para verificar después de reiniciar:${NC}"
echo "   ./check-services.sh"
echo ""