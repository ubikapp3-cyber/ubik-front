#!/usr/bin/env bash
# Ejecutar como root o con sudo
# Reemplaza los valores SUBDOMAIN, DUCKDNS_TOKEN y EMAIL antes de ejecutar.

SUBDOMAIN="ubik-back"               # e.g. mi-ubik
DUCKDNS_TOKEN="1d987fdd-0c42-4157-b9ca-38c17ec5505e"
EMAIL="juankos0714@gmail.com"                   # para certbot
DOMAIN="${SUBDOMAIN}.duckdns.org"

set -euo pipefail

echo "1/12 - Actualizando paquetes e instalando nginx, certbot y curl..."
apt update -y
apt install -y nginx curl software-properties-common

echo "2/12 - Instalando plugin de certbot para nginx..."
apt install -y certbot python3-certbot-nginx

echo "3/12 - Creando script de actualización de DuckDNS (/usr/local/bin/duckdns-update.sh)..."
cat > /usr/local/bin/duckdns-update.sh <<EOF
#!/usr/bin/env bash
SUB="${SUBDOMAIN}"
TOKEN="${DUCKDNS_TOKEN}"
URL="https://www.duckdns.org/update?domains=\${SUB}&token=\${TOKEN}&ip="
curl -s "\${URL}" >/var/log/duckdns-update.log 2>&1
EOF
chmod +x /usr/local/bin/duckdns-update.sh

echo "4/12 - Configurando systemd service y timer para actualizar DuckDNS cada 5 minutos..."
cat > /etc/systemd/system/duckdns-update.service <<'EOF'
[Unit]
Description=Update DuckDNS A record

[Service]
Type=oneshot
ExecStart=/usr/local/bin/duckdns-update.sh
EOF

cat > /etc/systemd/system/duckdns-update.timer <<'EOF'
[Unit]
Description=Run DuckDNS updater every 5 minutes

[Timer]
OnBootSec=30
OnUnitActiveSec=5min
Persistent=true

[Install]
WantedBy=timers.target
EOF

systemctl daemon-reload
systemctl enable --now duckdns-update.timer
systemctl start duckdns-update.service || true
echo "DuckDNS updater started (check /var/log/duckdns-update.log)."

echo "5/12 - Creando configuración de nginx para el dominio ${DOMAIN}..."
NGINX_CONF="/etc/nginx/sites-available/ubik_gateway.conf"
cat > "${NGINX_CONF}" <<EOF
server {
    listen 80;
    server_name ${DOMAIN};

    # Redirect root or other requests to the gateway
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;

        # for websockets
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_buffering off;
    }
}

# Optional: block access to Docker socket or internal files, adjust as needed.
EOF

ln -sf "${NGINX_CONF}" /etc/nginx/sites-enabled/ubik_gateway.conf
nginx -t
systemctl reload nginx

echo "6/12 - Permitir HTTP/HTTPS en UFW (si usas ufw)..."
if command -v ufw >/dev/null 2>&1; then
  ufw allow 'Nginx Full' || true
  echo "UFW rules updated."
fi

echo "7/12 - Comprobando que el gateway responde en localhost:8080 (desde VM)..."
if curl -sSfL --max-time 5 "http://127.0.0.1:8080/actuator/health" >/dev/null 2>&1; then
  echo "Gateway responde en localhost:8080"
else
  echo "AVISO: gateway no respondió en localhost:8080. Comprueba docker ps y logs antes de solicitar certificado."
fi

echo "8/12 - Ejecutando certbot para obtener certificado (Let's Encrypt) y configurar nginx..."
# Certbot necesita puerto 80 accesible desde internet para el challenge.
# --redirect añadirá redirect http->https
certbot --nginx -d "${DOMAIN}" --non-interactive --agree-tos --email "${EMAIL}" --redirect

echo "9/12 - Forzar reload de nginx..."
systemctl reload nginx

echo "10/12 - Mostrar status: NGINX and cert files"
systemctl status nginx --no-pager
echo "Certs (if created):"
ls -l /etc/letsencrypt/live/"${DOMAIN}" || true

echo "11/12 - Recordatorio: configura Vercel NEXT_PUBLIC_API_URL -> https://${DOMAIN}"
echo "12/12 - Hecho. Prueba desde tu máquina:"
echo "  curl -I https://${DOMAIN}/actuator/health"
echo
echo "Si obtienes 403/401/401: revisa los logs del gateway y que el endpoint /actuator/health esté permitido por SecurityConfig."
