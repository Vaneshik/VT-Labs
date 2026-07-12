#!/bin/bash

echo "===== MinIO Setup on Helios ====="
echo ""

if [ ! -f .env ]; then
    echo "❌ ERROR: .env file not found"
    exit 1
fi

source .env

SSH_PORT=${SSH_PORT:-2222}
SSH_USER=${SSH_USER:-s409858}
SSH_HOST=${SSH_HOST:-se.ifmo.ru}

# MinIO конфигурация для Helios
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001
MINIO_USER=${MINIO_ACCESS_KEY:-minioadmin}
MINIO_PASSWORD=${MINIO_SECRET_KEY:-minioadmin}
MINIO_BUCKET=${MINIO_BUCKET:-person-imports}

echo "Connecting to Helios..."
echo "User: $SSH_USER"
echo "Host: $SSH_HOST"
echo "Port: $SSH_PORT"
echo ""

ssh -p $SSH_PORT $SSH_USER@$SSH_HOST << ENDSSH
set -e

echo "Step 1: Checking MinIO installation..."

# Создать директории
mkdir -p ~/minio-data ~/minio-bin

cd ~/minio-bin

# Скачать MinIO если нет
if [ ! -f minio ]; then
    echo "Downloading MinIO..."
    wget https://dl.min.io/server/minio/release/linux-amd64/minio
    chmod +x minio
    echo "✅ MinIO downloaded"
else
    echo "✅ MinIO already exists"
fi

# Скачать MinIO Client если нет
if [ ! -f mc ]; then
    echo "Downloading MinIO Client..."
    wget https://dl.min.io/client/mc/release/linux-amd64/mc
    chmod +x mc
    echo "✅ MinIO Client downloaded"
else
    echo "✅ MinIO Client already exists"
fi

echo ""
echo "Step 2: Stopping old MinIO instance..."
pkill -9 -f "minio server" 2>/dev/null || true
sleep 2

echo ""
echo "Step 3: Starting MinIO server..."

# Запустить MinIO в фоне
export MINIO_ROOT_USER=$MINIO_USER
export MINIO_ROOT_PASSWORD=$MINIO_PASSWORD

nohup ./minio server ~/minio-data \\
    --address ":$MINIO_PORT" \\
    --console-address ":$MINIO_CONSOLE_PORT" \\
    > ~/minio.log 2>&1 &

MINIO_PID=\$!
echo "MinIO started with PID: \$MINIO_PID"

# Подождать запуска
echo "Waiting for MinIO to start..."
sleep 5

# Проверить что MinIO запущен
if ps -p \$MINIO_PID > /dev/null; then
    echo "✅ MinIO is running"
else
    echo "❌ MinIO failed to start. Check ~/minio.log"
    exit 1
fi

echo ""
echo "Step 4: Configuring MinIO client..."

# Настроить mc
./mc alias set helios-minio http://localhost:$MINIO_PORT $MINIO_USER $MINIO_PASSWORD

echo ""
echo "Step 5: Creating bucket..."

# Создать bucket
./mc mb helios-minio/$MINIO_BUCKET --ignore-existing

# Установить публичную политику для скачивания
./mc anonymous set download helios-minio/$MINIO_BUCKET

echo ""
echo "Step 6: Verifying setup..."

# Проверить bucket
./mc ls helios-minio/

echo ""
echo "===== MinIO Setup Complete! ====="
echo ""
echo "MinIO Server: http://localhost:$MINIO_PORT"
echo "MinIO Console: http://localhost:$MINIO_CONSOLE_PORT"
echo "Bucket: $MINIO_BUCKET"
echo ""
echo "Username: $MINIO_USER"
echo "Password: $MINIO_PASSWORD"
echo ""
echo "To check status:"
echo "  ps aux | grep minio"
echo "  tail -f ~/minio.log"
echo ""
echo "To stop MinIO:"
echo "  pkill -9 -f 'minio server'"
echo ""
ENDSSH

echo ""
echo "✅ MinIO setup on Helios completed successfully!"
echo ""
echo "Important: Update your .env file with:"
echo "  MINIO_ENDPOINT=http://localhost:$MINIO_PORT"
echo "  (MinIO runs on Helios, accessible via localhost)"
