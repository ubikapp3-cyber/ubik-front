#!/bin/bash

# ===========================================
# SCRIPT DE DESPLIEGUE - UBIK MICROSERVICES
# ===========================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  UBIK Microservices Deployment${NC}"
echo -e "${BLUE}========================================${NC}\n"

# ===========================================
# VERIFICAR PREREQUISITOS
# ===========================================
echo -e "${YELLOW}[1/6] Verificando prerequisitos...${NC}"

if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker no está instalado${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose no está instalado${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker: $(docker --version)${NC}"
echo -e "${GREEN}✓ Docker Compose: $(docker-compose --version)${NC}\n"

# ===========================================
# VERIFICAR ARCHIVO .env
# ===========================================
echo -e "${YELLOW}[2/6] Verificando configuración...${NC}"

if [ ! -f .env ]; then
    echo -e "${RED}❌ Archivo .env no encontrado${NC}"
    echo -e "${YELLOW}Copia .env.example a .env y configura las variables${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Archivo .env encontrado${NC}\n"

# ===========================================
# DETENER CONTENEDORES EXISTENTES
# ===========================================
echo -e "${YELLOW}[3/6] Deteniendo contenedores existentes...${NC}"
docker-compose down --remove-orphans 2>/dev/null || true
echo -e "${GREEN}✓ Contenedores detenidos${NC}\n"

# ===========================================
# LIMPIAR IMÁGENES ANTIGUAS (OPCIONAL)
# ===========================================
read -p "¿Deseas limpiar imágenes antiguas? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}[4/6] Limpiando imágenes antiguas...${NC}"
    docker system prune -f
    echo -e "${GREEN}✓ Limpieza completada${NC}\n"
else
    echo -e "${BLUE}[4/6] Omitiendo limpieza de imágenes${NC}\n"
fi

# ===========================================
# CONSTRUIR IMÁGENES
# ===========================================
echo -e "${YELLOW}[5/6] Construyendo imágenes Docker...${NC}"
docker-compose build --no-cache

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Imágenes construidas exitosamente${NC}\n"
else
    echo -e "${RED}❌ Error construyendo imágenes${NC}"
    exit 1
fi

# ===========================================
# INICIAR SERVICIOS
# ===========================================
echo -e "${YELLOW}[6/6] Iniciando servicios...${NC}"
docker-compose up -d

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Servicios iniciados${NC}\n"
else
    echo -e "${RED}❌ Error iniciando servicios${NC}"
    exit 1
fi

# ===========================================
# VERIFICAR ESTADO DE SERVICIOS
# ===========================================
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Estado de Servicios${NC}"
echo -e "${BLUE}========================================${NC}\n"

sleep 10  # Dar tiempo a los servicios para iniciar

docker-compose ps

# ===========================================
# MOSTRAR LOGS
# ===========================================
echo -e "\n${YELLOW}Monitoreando logs (Ctrl+C para salir)...${NC}\n"
read -p "¿Deseas ver los logs? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker-compose logs -f
fi

# ===========================================
# INFORMACIÓN FINAL
# ===========================================
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  ✓ Despliegue Completado${NC}"
echo -e "${GREEN}========================================${NC}\n"

echo -e "${BLUE}Servicios disponibles:${NC}"
echo -e "  • Gateway:        http://localhost:8080"
echo -e "  • UserManagement: http://localhost:8081"
echo -e "  • MotelManagement: http://localhost:8083"
echo -e "  • Notifications:  http://localhost:8084"
echo -e "  • PostgreSQL:     localhost:5432"
echo -e "\n${BLUE}Comandos útiles:${NC}"
echo -e "  • Ver logs:       docker-compose logs -f"
echo -e "  • Ver estado:     docker-compose ps"
echo -e "  • Detener:        docker-compose down"
echo -e "  • Reiniciar:      docker-compose restart <service>"
echo -e ""
