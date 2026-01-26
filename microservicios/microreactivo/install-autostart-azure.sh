#!/bin/bash

# =================================================================
# Script de instalación de auto-inicio para Azure VM
# Configura systemd para iniciar contenedores al arrancar la VM
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
echo -e "${BLUE}Para Azure VM${NC}"
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
# El script está en microreactivo, así que PROJECT_DIR es este directorio
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
    echo ""
    echo -e "${YELLOW}💡 Archivos encontrados en el directorio:${NC}"
    ls -la "$PROJECT_DIR" | grep -E '(docker-compose|yml|yaml)'
    exit 1
fi

# 1. Crear el script de auto-inicio
echo -e "${GREEN}📝 Creando script de auto-inicio...${NC}"

cat > /usr/local/bin/docker-autostart.sh << 'SCRIPT_CONTENT'
#!/bin/bash

# Script de auto-inicio de contenedores Docker
# Ejecutado automáticamente por systemd al iniciar la VM

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

# Cambiar al directorio del proyecto
cd PROJECT_DIR_PLACEHOLDER || {
    log "ERROR: No se pudo acceder al directorio del proyecto"
    exit 1
}

log "📂 Directorio de trabajo: $(pwd)"

# Cargar variables de entorno si existe .env
if [ -f .env ]; then
    log "📋 Cargando variables de entorno desde .env"
    export $(cat .env | grep -v '^#' | xargs)
fi

# Iniciar todos los servicios
log "🚀 Iniciando servicios con docker-compose..."
docker-compose up -d 2>&1 | tee -a "$LOG_FILE"

if [ ${PIPESTATUS[0]} -eq 0 ]; then
    log "✅ Servicios iniciados correctamente"
else
    log "❌ Error al iniciar servicios"
    exit 1
fi

# Esperar a que PostgreSQL esté listo
log "⏳ Esperando a que PostgreSQL esté listo..."
MAX_ATTEMPTS=30
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    if docker exec motel-postgres-db pg_isready -U postgres -d motel_management_db >/dev/null 2>&1; then
        log "✅ PostgreSQL está listo"
        break
    fi
    
    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        log "⚠️  PostgreSQL tardó mucho en iniciarse"
        break
    fi
    
    log "Esperando PostgreSQL... intento $ATTEMPT/$MAX_ATTEMPTS"
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

# Esperar a que los servicios estén listos
log "⏳ Esperando a que los microservicios estén listos..."
sleep 15

# Verificar health de los servicios
check_health() {
    local service_name=$1
    local port=$2
    local max_attempts=20
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:$port/actuator/health >/dev/null 2>&1; then
            log "✅ $service_name está listo"
            return 0
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            log "⚠️  $service_name no respondió después de $max_attempts intentos"
            return 1
        fi
        
        sleep 3
        attempt=$((attempt + 1))
    done
}

# Verificar servicios
check_health "Gateway" "8080"
check_health "UserManagement" "8081"
check_health "MotelManagement" "8083"

# Mostrar estado de contenedores
log "📊 Estado de contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | tee -a "$LOG_FILE"

log "=========================================="
log "✅ Sistema iniciado correctamente"
log "=========================================="

# Mostrar información útil
log ""
log "🌐 Servicios disponibles:"
log "   - Gateway: http://localhost:8080"
log "   - UserManagement: http://localhost:8081"
log "   - MotelManagement: http://localhost:8083"
log "   - PostgreSQL: localhost:5432"
log "   - Adminer: http://localhost:8081"
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
Description=Auto-inicio de contenedores Docker para Ubik App
After=docker.service
Requires=docker.service
Wants=network-online.target
After=network-online.target

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/local/bin/docker-autostart.sh
User=root
StandardOutput=journal
StandardError=journal
TimeoutStartSec=300

# Reintentar en caso de fallo
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

echo -e "${GREEN}✅ Servicio creado en /etc/systemd/system/docker-autostart.service${NC}"

# 3. Habilitar el servicio
echo ""
echo -e "${GREEN}⚙️  Habilitando servicio...${NC}"

systemctl daemon-reload
systemctl enable docker-autostart.service

echo -e "${GREEN}✅ Servicio habilitado${NC}"

# 4. Crear archivo de log
touch /var/log/docker-autostart.log
chmod 666 /var/log/docker-autostart.log

# 5. Crear script de verificación para el usuario
cat > $USER_HOME/check-docker-services.sh << 'EOF'
#!/bin/bash

# Script para verificar el estado de los servicios Docker

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}🔍 Verificando servicios Docker${NC}"
echo "=============================================="
echo ""

# Estado del servicio systemd
echo -e "${YELLOW}📋 Estado del servicio de auto-inicio:${NC}"
sudo systemctl status docker-autostart --no-pager
echo ""

# Contenedores corriendo
echo -e "${YELLOW}🐳 Contenedores corriendo:${NC}"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ""

# Health checks
echo -e "${YELLOW}🏥 Health checks:${NC}"

check_service() {
    local name=$1
    local port=$2
    
    if curl -s http://localhost:$port/actuator/health >/dev/null 2>&1; then
        echo -e "  ${GREEN}✅ $name (puerto $port) - OK${NC}"
    else
        echo -e "  ${RED}❌ $name (puerto $port) - No responde${NC}"
    fi
}

check_service "Gateway" "8080"
check_service "UserManagement" "8081"
check_service "MotelManagement" "8083"

echo ""
echo -e "${YELLOW}📝 Últimas líneas del log:${NC}"
sudo tail -20 /var/log/docker-autostart.log
echo ""
echo "=============================================="
echo -e "${BLUE}💡 Comandos útiles:${NC}"
echo "  sudo systemctl status docker-autostart  - Ver estado del servicio"
echo "  sudo journalctl -u docker-autostart -f - Ver logs en tiempo real"
echo "  sudo tail -f /var/log/docker-autostart.log - Ver log del script"
echo "  docker ps - Ver contenedores corriendo"
echo "  docker logs <nombre-contenedor> - Ver logs de un contenedor"
echo ""
EOF

chmod +x $USER_HOME/check-docker-services.sh
chown $REAL_USER:$REAL_USER $USER_HOME/check-docker-services.sh

echo ""
echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}✅ Instalación completada exitosamente${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""
echo -e "${YELLOW}📋 Lo que se ha configurado:${NC}"
echo ""
echo "  1. ✅ Script de auto-inicio en /usr/local/bin/docker-autostart.sh"
echo "  2. ✅ Servicio systemd: docker-autostart.service"
echo "  3. ✅ Auto-inicio habilitado (se ejecutará al reiniciar la VM)"
echo "  4. ✅ Script de verificación en $USER_HOME/check-docker-services.sh"
echo ""
echo -e "${YELLOW}🔧 Comandos útiles:${NC}"
echo ""
echo "  # Ver estado del servicio:"
echo "  sudo systemctl status docker-autostart"
echo ""
echo "  # Ver logs del servicio:"
echo "  sudo journalctl -u docker-autostart -f"
echo ""
echo "  # Ver logs del script:"
echo "  sudo tail -f /var/log/docker-autostart.log"
echo ""
echo "  # Iniciar manualmente (sin reiniciar):"
echo "  sudo systemctl start docker-autostart"
echo ""
echo "  # Detener el servicio:"
echo "  sudo systemctl stop docker-autostart"
echo ""
echo "  # Deshabilitar auto-inicio:"
echo "  sudo systemctl disable docker-autostart"
echo ""
echo "  # Verificar estado de servicios:"
echo "  ./check-docker-services.sh"
echo ""
echo -e "${GREEN}🔄 Los contenedores se iniciarán automáticamente al reiniciar la VM${NC}"
echo ""
echo -e "${YELLOW}💡 Puedes probar ahora con:${NC}"
echo "   sudo systemctl start docker-autostart"
echo ""
echo -e "${YELLOW}📊 O verificar el estado con:${NC}"
echo "   ./check-docker-services.sh"
echo ""
