#!/bin/bash
# =================================================================
# UBIK — Instalador del servicio systemd
# Ejecutar como root o con sudo: sudo bash install.sh
# =================================================================

set -e

PROJECT_PATH="$HOME/ubik-front/microservicios/microreactivo"

echo "Copiando ubik-boot.sh al proyecto..."
cp ubik-boot.sh "$PROJECT_PATH/ubik-boot.sh"
chmod +x "$PROJECT_PATH/ubik-boot.sh"

echo "Instalando ubik.service en systemd..."
cp ubik.service /etc/systemd/system/ubik.service

echo "Recargando systemd..."
systemctl daemon-reload

echo "Habilitando ubik.service (arranca automáticamente en cada boot)..."
systemctl enable ubik.service

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✔  Instalación completa."
echo ""
echo "Comandos útiles:"
echo "  sudo systemctl start ubik      → Arrancar ahora (sin reiniciar)"
echo "  sudo systemctl stop ubik       → Detener todos los contenedores"
echo "  sudo systemctl status ubik     → Ver estado del servicio"
echo "  sudo journalctl -u ubik -f     → Ver logs en tiempo real"
echo "  sudo tail -f /var/log/ubik-boot.log  → Log del boot script"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
