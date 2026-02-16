#!/bin/bash

# Script de solución rápida para notification-service unhealthy

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   Solución Rápida - Notification Service  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
echo ""

# Función para verificar salud
check_health() {
    local service=$1
    local port=$2
    local max_attempts=20
    local attempt=1
    
    echo -e "${YELLOW}🔍 Verificando $service en puerto $port...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f http://localhost:$port/actuator/health >/dev/null 2>&1; then
            echo -e "${GREEN}✅ $service está saludable${NC}"
            return 0
        fi
        
        echo -e "   Intento $attempt/$max_attempts..."
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service no respondió después de $max_attempts intentos${NC}"
    return 1
}

# Paso 1: Ver estado actual
echo -e "${YELLOW}[Paso 1/5] Estado actual:${NC}"
docker ps --format "table {{.Names}}\t{{.Status}}"
echo ""

# Paso 2: Ver logs de notification-service
echo -e "${YELLOW}[Paso 2/5] Últimas líneas de logs:${NC}"
docker logs notification-service --tail=20
echo ""

# Paso 3: Detener gateway si existe
echo -e "${YELLOW}[Paso 3/5] Limpiando gateway fallido...${NC}"
docker-compose rm -f gateway 2>/dev/null || true
echo ""

# Paso 4: Reiniciar notification-service
echo -e "${YELLOW}[Paso 4/5] Reiniciando notification-service...${NC}"
docker-compose restart notification-service

echo -e "${YELLOW}⏳ Esperando 60 segundos para que el servicio inicie...${NC}"
sleep 60

# Verificar salud
if check_health "notification-service" "8084"; then
    echo ""
    echo -e "${GREEN}✅ Notification service está saludable${NC}"
    
    # Paso 5: Intentar iniciar gateway
    echo ""
    echo -e "${YELLOW}[Paso 5/5] Intentando iniciar gateway...${NC}"
    docker-compose up -d gateway
    
    echo -e "${YELLOW}⏳ Esperando 30 segundos...${NC}"
    sleep 30
    
    if check_health "gateway" "8080"; then
        echo ""
        echo -e "${GREEN}╔════════════════════════════════════════════╗${NC}"
        echo -e "${GREEN}║   ✅ TODOS LOS SERVICIOS SALUDABLES       ║${NC}"
        echo -e "${GREEN}╚════════════════════════════════════════════╝${NC}"
        echo ""
        docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        echo ""
        echo -e "${GREEN}🎉 Sistema funcionando correctamente${NC}"
    else
        echo ""
        echo -e "${RED}❌ Gateway sigue teniendo problemas${NC}"
        echo -e "${YELLOW}Ver logs con: docker logs gateway${NC}"
    fi
else
    echo ""
    echo -e "${RED}❌ Notification service sigue unhealthy${NC}"
    echo ""
    echo -e "${YELLOW}Posibles causas:${NC}"
    echo "1. Error de autenticación Gmail (MAIL_PASSWORD incorrecto)"
    echo "2. Falta endpoint /actuator/health"
    echo "3. Puerto 8084 ocupado o inaccesible"
    echo ""
    echo -e "${YELLOW}Soluciones:${NC}"
    echo ""
    echo "A) Verificar configuración Gmail:"
    echo "   - Debe ser App Password de 16 caracteres"
    echo "   - Generar en: https://myaccount.google.com/apppasswords"
    echo ""
    echo "B) Ver logs detallados:"
    echo "   docker logs -f notification-service"
    echo ""
    echo "C) Probar health endpoint manualmente:"
    echo "   curl -v http://localhost:8084/actuator/health"
    echo ""
    echo "D) Verificar que spring-boot-starter-actuator esté en pom.xml"
    echo ""
    echo "E) Reinicio completo:"
    echo "   docker-compose down"
    echo "   docker-compose build --no-cache notification-service"
    echo "   docker-compose up -d"
fi

echo ""
echo -e "${YELLOW}💡 Para ver logs en tiempo real:${NC}"
echo "   docker logs -f notification-service"
echo ""
