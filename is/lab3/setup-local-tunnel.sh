#!/bin/bash

set -e

if [ ! -f .env ]; then
    echo "Ошибка: файл .env не найден"
    exit 1
fi

source .env

SSH_PORT=${SSH_PORT:-2222}
LOCAL_PORT=${REACT_APP_BACKEND_PORT:-32337}
REMOTE_PORT=${REACT_APP_BACKEND_PORT:-32337}
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001

echo "=== Настройка SSH туннеля для локальной разработки ==="
echo "Пробросы:"
echo "  localhost:${LOCAL_PORT} -> ${SSH_HOST}:${REMOTE_PORT} (Backend API)"
echo "  localhost:${MINIO_PORT} -> ${SSH_HOST}:${MINIO_PORT} (MinIO API)"
echo "  localhost:${MINIO_CONSOLE_PORT} -> ${SSH_HOST}:${MINIO_CONSOLE_PORT} (MinIO Console)"

pkill -f "ssh.*${SSH_HOST}.*${LOCAL_PORT}:localhost:${REMOTE_PORT}" 2>/dev/null || true
sleep 1

# Проверить и освободить порты
for port in ${LOCAL_PORT} ${MINIO_PORT} ${MINIO_CONSOLE_PORT}; do
    if lsof -Pi :${port} -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo "Порт ${port} занят, освобождаю..."
        lsof -ti :${port} | xargs kill -9 2>/dev/null || true
    fi
done
sleep 1

echo "Создаю SSH туннели..."
ssh -f -N \
    -o ServerAliveInterval=60 \
    -o ServerAliveCountMax=3 \
    -o ExitOnForwardFailure=yes \
    -L ${LOCAL_PORT}:localhost:${REMOTE_PORT} \
    -L ${MINIO_PORT}:localhost:${MINIO_PORT} \
    -L ${MINIO_CONSOLE_PORT}:localhost:${MINIO_CONSOLE_PORT} \
    -p ${SSH_PORT} \
    ${SSH_USER}@${SSH_HOST}

if [ $? -eq 0 ]; then
    echo "✓ SSH туннели установлены"
    sleep 2
    
    echo ""
    echo "Проверяю доступность сервисов..."
    
    # Проверка Backend API
    if curl -s http://localhost:${LOCAL_PORT}/person-management-system/api/persons > /dev/null 2>&1; then
        echo "✓ Backend API доступен"
    else
        echo "⚠ Backend API не отвечает, проверьте что WildFly запущен на helios"
    fi
    
    # Проверка MinIO
    if curl -s http://localhost:${MINIO_PORT}/minio/health/live > /dev/null 2>&1; then
        echo "✓ MinIO доступен"
    else
        echo "⚠ MinIO не отвечает, проверьте что MinIO запущен на helios"
    fi
else
    echo "✗ Ошибка создания SSH туннелей"
    exit 1
fi

echo ""
echo "=== SSH туннели готовы ==="
echo ""
echo "Backend:"
echo "  API: http://localhost:${LOCAL_PORT}/person-management-system/api"
echo "  WebSocket: ws://localhost:${LOCAL_PORT}/person-management-system/ws/persons"
echo ""
echo "MinIO:"
echo "  API: http://localhost:${MINIO_PORT}"
echo "  Console: http://localhost:${MINIO_CONSOLE_PORT}"
echo "  Credentials: minioadmin / minioadmin"
echo ""
echo "Для остановки туннелей выполните:"
echo "  pkill -f 'ssh.*${SSH_HOST}.*${LOCAL_PORT}'"
