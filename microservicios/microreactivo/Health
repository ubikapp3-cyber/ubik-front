#!/bin/bash

# ===========================================
# SCRIPT DE MONITOREO - UBIK MICROSERVICES
# Azure VM con PostgreSQL Flexible Server
# ===========================================

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  UBIK Health Check Dashboard${NC}"
echo -e "${BLUE}  Azure VM + Flexible Server${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Function to check service health
check_health() {
    local service_name=$1
    local port=$2
    local endpoint=$3
    
    echo -n "  $service_name (port $port): "
    
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port$endpoint 2>/dev/null)
    
    if [ "$response" = "200" ]; then
        echo -e "${GREEN}✓ HEALTHY${NC}"
        return 0
    else
        echo -e "${RED}✗ UNHEALTHY (HTTP $response)${NC}"
        return 1
    fi
}

# ===========================================
# CHECK ALL SERVICES
# ===========================================
echo -e "${YELLOW}Checking microservices...${NC}\n"

total=0
healthy=0

# Gateway
check_health "Gateway" "8080" "/actuator/health" && ((healthy++))
((total++))

# User Management
check_health "User Management" "8081" "/actuator/health" && ((healthy++))
((total++))

# Motel Management
check_health "Motel Management" "8083" "/actuator/health" && ((healthy++))
((total++))

# Notification Service
check_health "Notification Service" "8084" "/actuator/health" && ((healthy++))
((total++))

# ===========================================
# CHECK DATABASE CONNECTIVITY
# ===========================================
echo -e "\n${YELLOW}Checking external services...${NC}\n"

echo -n "  PostgreSQL (Azure Flexible Server): "

# Try to connect through one of the services that has DB access
db_check=$(curl -s http://localhost:8081/actuator/health 2>/dev/null | grep -o '"db".*"UP"')

if [ ! -z "$db_check" ]; then
    echo -e "${GREEN}✓ CONNECTED${NC}"
    ((healthy++))
else
    echo -e "${RED}✗ DISCONNECTED${NC}"
    echo -e "${YELLOW}    Tip: Check firewall rules in Azure Portal${NC}"
fi
((total++))

# ===========================================
# SUMMARY
# ===========================================
echo -e "\n${BLUE}========================================${NC}"
echo -e "${BLUE}  Summary${NC}"
echo -e "${BLUE}========================================${NC}\n"

if [ $healthy -eq $total ]; then
    echo -e "${GREEN}All services are healthy! ($healthy/$total)${NC}"
    exit 0
else
    echo -e "${YELLOW}Some services are unhealthy: $healthy/$total${NC}"
    
    echo -e "\n${YELLOW}Troubleshooting commands:${NC}"
    echo -e "  • View all logs:        docker-compose logs"
    echo -e "  • View service logs:    docker-compose logs <service-name>"
    echo -e "  • Check container status: docker-compose ps"
    echo -e "  • Restart service:      docker-compose restart <service-name>"
    echo -e "  • Check DB connection:  docker-compose logs usermanagement-service | grep -i database"
    echo ""
    exit 1
fi
