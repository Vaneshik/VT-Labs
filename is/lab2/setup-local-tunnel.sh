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

echo "=== Настройка SSH туннеля для локальной разработки ==="
echo "Проброс: localhost:${LOCAL_PORT} -> ${SSH_HOST}:${REMOTE_PORT}"

pkill -f "ssh.*${SSH_HOST}.*${LOCAL_PORT}:localhost:${REMOTE_PORT}" 2>/dev/null || true
sleep 1

if lsof -Pi :${LOCAL_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; then
    echo "Порт ${LOCAL_PORT} занят, освобождаю..."
    lsof -ti :${LOCAL_PORT} | xargs kill -9 2>/dev/null || true
    sleep 1
fi

echo "Создаю SSH туннель..."
ssh -f -N \
    -o ServerAliveInterval=60 \
    -o ServerAliveCountMax=3 \
    -o ExitOnForwardFailure=yes \
    -L ${LOCAL_PORT}:localhost:${REMOTE_PORT} \
    -p ${SSH_PORT} \
    ${SSH_USER}@${SSH_HOST}

if [ $? -eq 0 ]; then
    echo "✓ SSH туннель установлен"
    sleep 2
    
    echo ""
    echo "Проверяю доступность API..."
    if curl -s http://localhost:${LOCAL_PORT}/person-management-system/api/persons > /dev/null 2>&1; then
        echo "✓ API доступен: http://localhost:${LOCAL_PORT}/person-management-system/api"
    else
        echo "⚠ API не отвечает, проверьте что WildFly запущен на helios"
    fi
else
    echo "✗ Ошибка создания SSH туннеля"
    exit 1
fi

echo ""
echo "=== SSH туннель готов ==="
echo "API доступен на: http://localhost:${LOCAL_PORT}/person-management-system/api"
echo "WebSocket: ws://localhost:${LOCAL_PORT}/person-management-system/ws/persons"
echo ""
echo "Для остановки туннеля выполните:"
echo "  pkill -f 'ssh.*${SSH_HOST}.*${LOCAL_PORT}'"

