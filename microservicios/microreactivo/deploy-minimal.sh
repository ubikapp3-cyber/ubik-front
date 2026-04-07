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
docker compose -f docker-compose.minimal.yml down --remove-orphans 2>/dev/null || true

for port in 8080 8081 8083 8084 8085 8086 8087 8088; do
    sudo fuser -k "$port/tcp" 2>/dev/null || true
done

echo "🧹 Limpiando Docker..."
docker container prune -f
docker network prune -f
docker builder prune -f

echo "🌐 Creando red ubik-network..."
docker network create ubik-network 2>/dev/null || true

echo "⚙️ Configurando kernel..."
sudo sysctl -w net.ipv4.ip_forward=1
sudo iptables -P FORWARD ACCEPT

echo "🔥 Verificando Ollama + modelo..."
if ! systemctl is-active --quiet ollama; then
    sudo systemctl start ollama
    sleep 5
fi

if ! ollama list | grep -q "qwen2.5:1.5b"; then
    echo "📥 Descargando modelo qwen2.5:1.5b (~934 MB)..."
    ollama pull qwen2.5:1.5b
fi

# Eliminar modelo viejo para liberar espacio en disco
if ollama list | grep -q "smollm2:135m"; then
    echo "🗑️  Eliminando modelo antiguo smollm2:135m..."
    ollama rm smollm2:135m
fi

echo "✅ Ollama listo con qwen2.5:1.5b"

echo ""
echo "💳 Verificando Stripe CLI..."
if [ -z "${STRIPE_SECRET_KEY}" ]; then
    echo "⚠️  STRIPE_SECRET_KEY no está definida — stripe-cli no podrá autenticarse"
else
    echo "✅ STRIPE_SECRET_KEY presente"
fi
docker pull stripe/stripe-cli:latest
echo "✅ Imagen stripe/stripe-cli:latest lista"

if [[ "$1" == "--build" ]]; then
    echo ""
    echo "🏗️ Construyendo imágenes UNA POR UNA (evita timeout en 2 vCPUs)..."

    # Orden: de menor a mayor dependencia en el compose
    SERVICES=(
        "notification-service:notificationService/Dockerfile"
        "usermanagement-service:userManagement/Dockerfile"
        "streak-service:streakService/Dockerfile"
        "ai-service:ai-service/Dockerfile"
        "payment-service:paymentService/Dockerfile"
        "motel-management-service:motelManegement/Dockerfile"
        "gateway:gateway/Dockerfile"
    )

    for entry in "${SERVICES[@]}"; do
        NAME="${entry%%:*}"
        DOCKERFILE="${entry##*:}"

        echo ""
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        echo "📦 Construyendo: $NAME"
        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

        docker build \
            --no-cache \
            -f "$DOCKERFILE" \
            -t "microreactivo-${NAME}:latest" \
            --progress=plain \
            . 2>&1 | tail -20

        STATUS=$?
        if [ $STATUS -ne 0 ]; then
            echo "❌ Falló build de $NAME (exit $STATUS)"
            exit 1
        fi

        echo "✅ $NAME construido OK"
        echo "⏸️  Pausa 10s para liberar RAM..."
        sleep 10
    done

    echo ""
    echo "✅ Todas las imágenes construidas"
    docker images | grep microreactivo
fi

echo ""
echo "🚀 Levantando sistema..."
docker compose -f docker-compose.minimal.yml up -d

echo ""
echo "⏳ Esperando que los servicios arranquen (90s)..."
sleep 90

echo ""
echo "📊 Estado de contenedores:"
docker compose -f docker-compose.minimal.yml ps

echo ""
echo "✅ Deploy completado"
echo "-------------------------------------------------------"
echo "🌐 Gateway:    http://localhost:8080"
echo "🤖 AI Svc:     http://localhost:8088"
echo "💳 Stripe CLI: $(docker inspect --format='{{.State.Status}}' stripe-cli 2>/dev/null || echo 'no iniciado')"
echo ""
echo "📊 RAM actual:"
free -h | awk 'NR<=2'
echo ""
echo "🤖 Ollama:"
ollama ps 2>/dev/null || echo "   (modelo no cargado aún)"
echo "-------------------------------------------------------"
