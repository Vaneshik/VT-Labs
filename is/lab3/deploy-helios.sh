#!/bin/bash

set -e

echo "===== Deploying Person Management System to Helios ====="
echo ""

if [ ! -f .env ]; then
    echo "❌ Ошибка: файл .env не найден"
    exit 1
fi

source .env

if [ -z "$SSH_USER" ] || [ -z "$SSH_HOST" ]; then
    echo "❌ Ошибка: SSH_USER или SSH_HOST не указаны"
    exit 1
fi

SSH_PORT=${SSH_PORT:-2222}
APP_NAME=${APP_NAME:-person-management-system}
DB_USER=${DB_USER:-$SSH_USER}
WILDFLY_PORT=${REACT_APP_BACKEND_PORT:-32337}
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001

echo "Configuration:"
echo "  SSH: $SSH_USER@$SSH_HOST:$SSH_PORT"
echo "  WildFly Port: $WILDFLY_PORT"
echo "  MinIO Port: $MINIO_PORT"
echo ""

# Шаг 1: Собрать проект
echo "Step 1: Building project..."
cp src/main/resources/hibernate.cfg.xml src/main/resources/hibernate.cfg.xml.backup

sed -i.tmp "s/YOUR_USERNAME/$DB_USER/g" src/main/resources/hibernate.cfg.xml
sed -i.tmp "s/YOUR_PASSWORD/$DB_PASSWORD/g" src/main/resources/hibernate.cfg.xml
rm -f src/main/resources/hibernate.cfg.xml.tmp

mvn clean package -DskipTests

mv src/main/resources/hibernate.cfg.xml.backup src/main/resources/hibernate.cfg.xml

WAR_FILE="target/${APP_NAME}.war"

if [ ! -f "$WAR_FILE" ]; then
    echo "❌ Ошибка: WAR файл не найден"
    exit 1
fi

echo "✅ Project built successfully"
echo ""

# Шаг 2: Настроить MinIO на Helios
echo "Step 2: Setting up MinIO on Helios..."

ssh -p $SSH_PORT $SSH_USER@$SSH_HOST "mkdir -p ~/webapps ~/sql ~/minio-data ~/minio-bin"

ssh -p $SSH_PORT $SSH_USER@$SSH_HOST << 'MINIO_SETUP'
cd ~/minio-bin

# Скачать MinIO если нет
if [ ! -f minio ]; then
    echo "Downloading MinIO..."
    wget -q https://dl.min.io/server/minio/release/linux-amd64/minio
    chmod +x minio
fi

# Скачать MinIO Client если нет
if [ ! -f mc ]; then
    echo "Downloading MinIO Client..."
    wget -q https://dl.min.io/client/mc/release/linux-amd64/mc
    chmod +x mc
fi

# Остановить старый MinIO
pkill -9 -f "minio server" 2>/dev/null || true
sleep 2

# Запустить MinIO
export MINIO_ROOT_USER=minioadmin
export MINIO_ROOT_PASSWORD=minioadmin

nohup ./minio server ~/minio-data \
    --address ":9000" \
    --console-address ":9001" \
    > ~/minio.log 2>&1 &

sleep 5

# Настроить bucket
./mc alias set local http://localhost:9000 minioadmin minioadmin 2>/dev/null || true
./mc mb local/person-imports --ignore-existing 2>/dev/null || true
./mc anonymous set download local/person-imports 2>/dev/null || true

echo "✅ MinIO configured and running"
MINIO_SETUP

echo "✅ MinIO setup complete"
echo ""

# Шаг 3: Деплой приложения
echo "Step 3: Deploying application..."

scp -P $SSH_PORT $WAR_FILE $SSH_USER@$SSH_HOST:~/webapps/
scp -P $SSH_PORT database-functions.sql test-data.sql $SSH_USER@$SSH_HOST:~/sql/ 2>/dev/null || true

ssh -p $SSH_PORT $SSH_USER@$SSH_HOST << ENDSSH
pkill -9 -f wildfly 2>/dev/null || true
sleep 5

cp ~/webapps/person-management-system.war ~/wildfly-preview-26.1.3.Final/standalone/deployments/

cd ~/wildfly-preview-26.1.3.Final
export _JAVA_OPTIONS="-Xmx1024M -XX:MaxMetaspaceSize=2048m"

nohup ./bin/standalone.sh -b 0.0.0.0 -bmanagement=0.0.0.0 > ~/wildfly.log 2>&1 &

sleep 40

curl -s http://localhost:$WILDFLY_PORT/person-management-system/api/persons > /dev/null
if [ \$? -eq 0 ]; then
    echo "✅ API is running on port $WILDFLY_PORT"
fi
ENDSSH

echo ""
echo "===== Deploy Complete! ====="
echo ""
echo "Services:"
echo "  Backend API: http://$SSH_HOST:$WILDFLY_PORT/person-management-system/api"
echo "  MinIO API: http://$SSH_HOST:$MINIO_PORT (via SSH tunnel)"
echo "  MinIO Console: http://$SSH_HOST:$MINIO_CONSOLE_PORT (via SSH tunnel)"
echo ""
echo "To access MinIO from your machine, create SSH tunnel:"
echo "  ssh -p $SSH_PORT -L 9000:localhost:9000 -L 9001:localhost:9001 $SSH_USER@$SSH_HOST"
echo ""
echo "Then access:"
echo "  MinIO API: http://localhost:9000"
echo "  MinIO Console: http://localhost:9001"
echo ""
echo "To check logs:"
echo "  ssh -p $SSH_PORT $SSH_USER@$SSH_HOST 'tail -f ~/wildfly.log'"
echo "  ssh -p $SSH_PORT $SSH_USER@$SSH_HOST 'tail -f ~/minio.log'"
echo ""

