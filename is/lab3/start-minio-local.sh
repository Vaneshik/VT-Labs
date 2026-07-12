#!/bin/bash

# Скрипт для запуска MinIO ЛОКАЛЬНО (т.к. на Helios FreeBSD не поддерживается)

set -e

echo "╔════════════════════════════════════════════════════╗"
echo "║   MinIO - ЛОКАЛЬНЫЙ запуск                        ║"
echo "║   (FreeBSD на Helios не поддерживается)           ║"
echo "╚════════════════════════════════════════════════════╝"
echo ""

# Проверка Docker
if ! docker ps &>/dev/null; then
    echo "❌ Docker не запущен! Запустите Docker Desktop"
    exit 1
fi

echo "▶️  Запуск MinIO через Docker..."
docker-compose up -d minio

# Ждем запуска
echo "⏳ Ждем готовности MinIO..."
sleep 5

# Проверка
if curl -s http://localhost:9000/minio/health/live > /dev/null 2>&1; then
    echo "✅ MinIO запущен локально"
else
    echo "⚠️  MinIO еще запускается..."
    sleep 5
fi

# Настройка bucket
echo "▶️  Настройка MinIO..."
./setup-minio.sh

echo ""
echo "╔════════════════════════════════════════════════════╗"
echo "║   ✅ MinIO готов!                                  ║"
echo "╚════════════════════════════════════════════════════╝"
echo ""
echo "MinIO Console: http://localhost:9001"
echo "  Username: minioadmin"
echo "  Password: minioadmin"
echo ""
echo "⚠️  ВАЖНО: Java приложение на Helios подключается к"
echo "   MinIO на localhost:9000 (туннель не нужен)"
echo ""
