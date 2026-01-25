#!/bin/bash

# =================================================================
# Instalador de auto-inicio de contenedores Docker
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
echo -e "${BLUE}================================================${NC}"
echo ""

# Verificar que se ejecute como root o con sudo
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}❌ Este script debe ejecutarse como root o con sudo${NC}"
    echo "Usa: sudo ./install-autostart.sh"
    exit 1
fi

# Obtener el usuario real (no root)
REAL_USER="${SUDO_USER:-$USER}"
USER_HOME=$(eval echo ~$REAL_USER)
PROJECT_DIR="$USER_HOME/Ubik-App/microservicios/microreactivo"

echo -e "${YELLOW}📂 Directorio del proyecto: $PROJECT_DIR${NC}"
echo -e "${YELLOW}👤 Usuario: $REAL_USER${NC}"
echo ""

# Verificar que el directorio del proyecto existe
if [ ! -d "$PROJECT_DIR" ]; then
    echo -e "${RED}❌ No se encontró el directorio del proyecto${NC}"
    echo "   Buscado en: $PROJECT_DIR"
    exit 1
fi

# 1. Copiar el script de auto-inicio
echo -e "${GREEN}📝 Creando script de auto-inicio...${NC}"

cat > /usr/local/bin/docker-autostart.sh << 'EOF'
#!/bin/bash

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

# Cambiar al directorio del proyecto
cd PROJECT_DIR_PLACEHOLDER || {
    log "ERROR: No se pudo acceder al directorio del proyecto"
    exit 1
}

# Iniciar contenedores
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

# Mostrar estado de contenedores
log "Estado de contenedores:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | tee -a "$LOG_FILE"

log "=========================================="
log "✅ Contenedores iniciados correctamente"
log "=========================================="
EOF

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
Description=Auto-inicio de contenedores Docker
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
ExecStart=/usr/local/bin/docker-autostart.sh
User=root
StandardOutput=journal
StandardError=journal

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

echo ""
echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}✅ Instalación completada exitosamente${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""
echo -e "${YELLOW}📋 Comandos útiles:${NC}"
echo ""
echo "  # Ver estado del servicio:"
echo "  sudo systemctl status docker-autostart"
echo ""
echo "  # Ver logs del servicio:"
echo "  sudo journalctl -u docker-autostart -f"
echo ""
echo "  # Ver logs del script:"
echo "  tail -f /var/log/docker-autostart.log"
echo ""
echo "  # Iniciar manualmente:"
echo "  sudo systemctl start docker-autostart"
echo ""
echo "  # Detener el servicio:"
echo "  sudo systemctl stop docker-autostart"
echo ""
echo "  # Deshabilitar auto-inicio:"
echo "  sudo systemctl disable docker-autostart"
echo ""
echo -e "${GREEN}🔄 Los contenedores se iniciarán automáticamente al reiniciar la VM${NC}"
echo ""
echo -e "${YELLOW}💡 Puedes probar ahora con:${NC}"
echo "   sudo systemctl start docker-autostart"
echo ""
