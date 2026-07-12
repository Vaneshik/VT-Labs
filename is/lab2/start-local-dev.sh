#!/bin/bash

set -e

if [ -f .env ]; then source .env; fi
BACKEND_PORT=${REACT_APP_BACKEND_PORT:-32337}

echo "=== Шаг 1/2: Настройка SSH туннеля ==="
./setup-local-tunnel.sh

echo ""
echo "=== Шаг 2/2: Запуск frontend (React Dev Server) ==="
echo ""
echo "Frontend будет доступен на: http://localhost:3000"
echo "API через туннель: http://localhost:${BACKEND_PORT}/person-management-system/api"
echo ""
echo "Нажмите Ctrl+C для остановки"
echo ""

cd frontend
export REACT_APP_BACKEND_HOST=localhost
export REACT_APP_BACKEND_PORT=${BACKEND_PORT}
npm start

