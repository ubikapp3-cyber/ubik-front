#!/bin/bash

# Script de diagnóstico para troubleshooting de Docker Compose

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   Diagnóstico Docker Compose              ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

# 1. Verificar Docker
echo -e "${YELLOW}[1/8] Verificando Docker...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker no está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker instalado: $(docker --version)${NC}"
echo ""

# 2. Verificar Docker Compose
echo -e "${YELLOW}[2/8] Verificando Docker Compose...${NC}"
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose no está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Docker Compose instalado: $(docker-compose --version)${NC}"
echo ""

# 3. Verificar docker-compose.yml
echo -e "${YELLOW}[3/8] Verificando docker-compose.yml...${NC}"
if [ ! -f docker-compose.yml ]; then
    echo -e "${RED}❌ Archivo docker-compose.yml no encontrado${NC}"
    echo -e "${YELLOW}Ubicación actual: $(pwd)${NC}"
    echo -e "${YELLOW}Archivos en este directorio:${NC}"
    ls -la
    exit 1
fi
echo -e "${GREEN}✅ docker-compose.yml encontrado${NC}"

# Validar sintaxis
echo -e "${YELLOW}   Validando sintaxis...${NC}"
if docker-compose config > /dev/null 2>&1; then
    echo -e "${GREEN}   ✅ Sintaxis correcta${NC}"
else
    echo -e "${RED}   ❌ Error de sintaxis en docker-compose.yml${NC}"
    echo -e "${YELLOW}   Mostrando error:${NC}"
    docker-compose config
    exit 1
fi
echo ""

# 4. Verificar archivo .env
echo -e "${YELLOW}[4/8] Verificando archivo .env...${NC}"
if [ ! -f .env ]; then
    echo -e "${RED}❌ Archivo .env no encontrado${NC}"
    echo -e "${YELLOW}💡 Crea el archivo .env con tus variables de entorno${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Archivo .env encontrado${NC}"

# Verificar variables críticas
echo -e "${YELLOW}   Verificando variables críticas...${NC}"
required_vars=("SPRING_R2DBC_URL" "SPRING_R2DBC_USERNAME" "SPRING_R2DBC_PASSWORD" "JWT_SECRET")
missing_vars=()

for var in "${required_vars[@]}"; do
    if ! grep -q "^${var}=" .env; then
        missing_vars+=("$var")
    fi
done

if [ ${#missing_vars[@]} -gt 0 ]; then
    echo -e "${RED}   ❌ Variables faltantes en .env:${NC}"
    for var in "${missing_vars[@]}"; do
        echo -e "${RED}      - $var${NC}"
    done
    exit 1
fi
echo -e "${GREEN}   ✅ Variables críticas presentes${NC}"
echo ""

# 5. Verificar Dockerfiles
echo -e "${YELLOW}[5/8] Verificando Dockerfiles...${NC}"
dockerfiles=(
    "gateway/Dockerfile"
    "userManagement/Dockerfile"
    "motelManegement/Dockerfile"
    "notificationService/Dockerfile"
)

for dockerfile in "${dockerfiles[@]}"; do
    if [ ! -f "$dockerfile" ]; then
        echo -e "${RED}   ❌ No encontrado: $dockerfile${NC}"
    else
        echo -e "${GREEN}   ✅ $dockerfile${NC}"
    fi
done
echo ""

# 6. Verificar conectividad a PostgreSQL Azure
echo -e "${YELLOW}[6/8] Verificando conectividad a Azure PostgreSQL...${NC}"

# Extraer host de .env
DB_HOST=$(grep SPRING_R2DBC_URL .env | cut -d'/' -f3 | cut -d':' -f1)
if [ ! -z "$DB_HOST" ]; then
    echo -e "${YELLOW}   Host: $DB_HOST${NC}"
    
    if timeout 5 bash -c "cat < /dev/null > /dev/tcp/$DB_HOST/5432" 2>/dev/null; then
        echo -e "${GREEN}   ✅ Conectividad a PostgreSQL OK${NC}"
    else
        echo -e "${RED}   ❌ No se puede conectar a PostgreSQL${NC}"
        echo -e "${YELLOW}   💡 Verifica:${NC}"
        echo -e "${YELLOW}      1. Firewall de Azure: Debe permitir IP de esta VM${NC}"
        echo -e "${YELLOW}      2. Reglas de red del servidor PostgreSQL${NC}"
        echo -e "${YELLOW}      3. Conectividad de red de la VM${NC}"
    fi
else
    echo -e "${RED}   ❌ No se pudo extraer host de .env${NC}"
fi
echo ""

# 7. Verificar imágenes Docker
echo -e "${YELLOW}[7/8] Verificando imágenes Docker...${NC}"
if docker images | grep -q "microreactivo"; then
    echo -e "${GREEN}   Imágenes existentes:${NC}"
    docker images | grep "microreactivo"
else
    echo -e "${YELLOW}   ⚠️  No hay imágenes construidas aún${NC}"
    echo -e "${YELLOW}   💡 Se construirán al ejecutar docker-compose up${NC}"
fi
echo ""

# 8. Intentar listar servicios en docker-compose.yml
echo -e "${YELLOW}[8/8] Servicios definidos en docker-compose.yml:${NC}"
docker-compose config --services
echo ""

# Resumen y recomendaciones
echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   Resumen y Recomendaciones                ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${YELLOW}📋 Estado actual:${NC}"
echo "   • Docker: Instalado"
echo "   • Docker Compose: Instalado"
echo "   • docker-compose.yml: Presente y válido"
echo "   • .env: Presente"
echo ""

echo -e "${YELLOW}🚀 Siguientes pasos:${NC}"
echo ""
echo "1. Si TODO está OK, construir e iniciar servicios:"
echo -e "   ${GREEN}docker-compose build --no-cache${NC}"
echo -e "   ${GREEN}docker-compose up -d${NC}"
echo ""
echo "2. Ver logs durante el inicio:"
echo -e "   ${GREEN}docker-compose up${NC} (sin -d para ver logs en tiempo real)"
echo ""
echo "3. Si hay errores, ver logs específicos:"
echo -e "   ${GREEN}docker-compose logs <nombre-servicio>${NC}"
echo ""
echo "4. Verificar estado después de iniciar:"
echo -e "   ${GREEN}docker ps${NC}"
echo -e "   ${GREEN}docker-compose ps${NC}"
echo ""

# Verificar si hay contenedores detenidos
stopped_containers=$(docker ps -a -q -f status=exited)
if [ ! -z "$stopped_containers" ]; then
    echo -e "${YELLOW}⚠️  Hay contenedores detenidos:${NC}"
    docker ps -a --format "table {{.Names}}\t{{.Status}}"
    echo ""
    echo -e "${YELLOW}💡 Limpia contenedores antiguos:${NC}"
    echo -e "   ${GREEN}docker-compose down${NC}"
    echo -e "   ${GREEN}docker system prune -f${NC}"
    echo ""
fi
