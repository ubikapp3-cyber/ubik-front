#!/bin/bash
set -e

PROJECT_PATH="$HOME/ubik-front/microservicios/microreactivo"
cd "$PROJECT_PATH"

echo "⏳ Esperando DNS..."
until nslookup duckdns.org > /dev/null 2>&1; do sleep 3; done
echo "✅ DNS listo"

echo "🔍 Sincronizando código..."
git pull origin respaldo

echo "💀 Deteniendo stack actual..."
sudo docker compose -f docker-compose.minimal.yml down --remove-orphans 2>/dev/null || true
sudo pkill -9 java 2>/dev/null || true

for port in 8080 8081 8083 8084 8085 8086 8087 8088; do
    sudo fuser -k "$port/tcp" 2>/dev/null || true
done

echo "🧹 Limpiando Docker..."
sudo docker container prune -f
sudo docker network prune -f

echo "🌐 Creando red ubik-network..."
sudo docker network create ubik-network 2>/dev/null || true

echo "⚙️ Configurando kernel..."
sudo sysctl -w net.ipv4.ip_forward=1
sudo iptables -P FORWARD ACCEPT

echo "🔥 Verificando Ollama + modelo..."
if ! systemctl is-active --quiet ollama; then
    sudo systemctl start ollama
    sleep 5
fi

# Verificar modelo descargado
if ! ollama list | grep -q "smollm2:135m"; then
    echo "📥 Descargando modelo smollm2:135m..."
    ollama pull smollm2:135m
fi

echo "✅ Ollama listo con smollm2:135m"

IMAGE_COUNT=$(sudo docker images | grep "microreactivo" | wc -l)
if [[ "$1" == "--build" ]] || [ "$IMAGE_COUNT" -lt 5 ]; then
    echo "🏗️ Construyendo imágenes..."
    sudo docker compose -f docker-compose.minimal.yml build --no-cache
else
    echo "✔️ Usando imágenes existentes ($IMAGE_COUNT). Usa --build para reconstruir."
fi

echo "🚀 Levantando sistema..."
sudo docker compose -f docker-compose.minimal.yml up -d

echo ""
echo "✅ Deploy completado"
echo "-------------------------------------------------------"
echo "🌐 Gateway:  http://localhost:8080"
echo "🤖 AI Svc:   http://localhost:8088"
echo "📊 RAM aprox usada por Ollama:"
ollama ps 2>/dev/null || echo "   (modelo no cargado aún, se cargará al primer request)"
echo "-------------------------------------------------------"
