#!/bin/bash

# =================================================================
# 🚀 UBIK - SCRIPT DE DESPLIEGUE MAESTRO (APP + MONITORING)
# =================================================================

PROJECT_PATH="$HOME/ubik-front/microservicios/microreactivo"
# Puertos base + Puertos de Monitoreo (3000, 9090, 3100, 9093, 8086)
PORTS=(8080 8081 8083 8084 8085 4040 3000 9090 3100 9093 8086)
COMPOSE_FILES="-f docker-compose.yml -f docker-compose.monitoring.yml"

cd "$PROJECT_PATH" || exit 1
echo "⏳ Esperando DNS..."
until nslookup duckdns.org > /dev/null 2>&1; do
    sleep 3
done
echo "✅ DNS listo"

echo "🔍 Sincronizando código desde Git..."
git pull origin respaldo

echo "💀 Deteniendo stack actual y limpiando procesos..."
# Usar docker compose down para una limpieza ordenada de redes y contenedores
sudo docker compose $COMPOSE_FILES down --remove-orphans

# Matar procesos Java residuales
sudo pkill -9 java 2>/dev/null || true

# Liberar puertos bloqueados
for port in "${PORTS[@]}"; do
    sudo fuser -k "$port/tcp" 2>/dev/null || true
done

echo "🧹 Limpieza profunda de Docker..."
sudo docker container prune -f
sudo docker network prune -f

echo "🌐 Configurando red interna 'ubik-network'..."
# Forzamos la creación de la red con el nombre exacto que usan los YAML
sudo docker network create ubik-network 2>/dev/null || true

echo "⚙️ Configurando Kernel para Docker..."
sudo sysctl -w net.ipv4.ip_forward=1
sudo iptables -P FORWARD ACCEPT

# Lógica de construcción inteligente
# Ahora tenemos más imágenes por el stack de monitoreo (~10)
IMAGE_COUNT=$(sudo docker images | grep "microreactivo" | wc -l)

# Si el usuario pasa --build como argumento, forzamos reconstrucción
if [[ "$1" == "--build" ]] || [ "$IMAGE_COUNT" -lt 5 ]; then
    echo "🏗️ Construyendo imágenes (esto puede tardar unos minutos)..."
    sudo docker compose $COMPOSE_FILES build --no-cache
else
    echo "✔️ Imágenes existentes ($IMAGE_COUNT), saltando construcción masiva."
    echo "💡 Tip: Usa './deploy-azure.sh --build' para forzar una reconstrucción total."
fi

echo "🚀 Levantando sistema completo (App + Monitoreo)..."
sudo docker compose $COMPOSE_FILES up -d

echo "✅ ¡Despliegue completado con éxito!"
echo "-------------------------------------------------------"
echo "🌐 Gateway: http://localhost:8080"
echo "📊 Grafana: http://localhost:3000 (admin/admin)"
echo "🔍 Prometheus: http://localhost:9090"
echo "-------------------------------------------------------"
