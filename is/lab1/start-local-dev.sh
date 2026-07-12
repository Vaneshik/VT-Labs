#!/bin/bash

set -e

echo "╔════════════════════════════════════════════════════════════╗"
echo "║     Локальная разработка Person Management System         ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""

echo "=== Шаг 1/2: Настройка SSH туннеля ==="
./setup-local-tunnel.sh

echo ""
echo "=== Шаг 2/2: Запуск frontend (React Dev Server) ==="
echo ""
echo "Frontend будет доступен на: http://localhost:3000"
echo "API через туннель: http://localhost:31337/person-management-system/api"
echo ""
echo "Нажмите Ctrl+C для остановки"
echo ""

cd frontend
npm start

