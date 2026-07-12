#!/bin/bash

set -e

if [ ! -f .env ]; then
    echo "Ошибка: файл .env не найден"
    exit 1
fi

source .env

if [ -z "$SSH_USER" ] || [ -z "$SSH_HOST" ]; then
    echo "Ошибка: SSH_USER или SSH_HOST не указаны"
    exit 1
fi

SSH_PORT=${SSH_PORT:-2222}
APP_NAME=${APP_NAME:-person-management-system}
DB_USER=${DB_USER:-$SSH_USER}
WILDFLY_PORT=${REACT_APP_BACKEND_PORT:-32337}

cp src/main/resources/hibernate.cfg.xml src/main/resources/hibernate.cfg.xml.backup

sed -i.tmp "s/YOUR_USERNAME/$DB_USER/g" src/main/resources/hibernate.cfg.xml
sed -i.tmp "s/YOUR_PASSWORD/$DB_PASSWORD/g" src/main/resources/hibernate.cfg.xml
rm -f src/main/resources/hibernate.cfg.xml.tmp

mvn clean package -DskipTests

mv src/main/resources/hibernate.cfg.xml.backup src/main/resources/hibernate.cfg.xml

WAR_FILE="target/${APP_NAME}.war"

if [ ! -f "$WAR_FILE" ]; then
    echo "Ошибка: WAR файл не найден"
    exit 1
fi

ssh -p $SSH_PORT $SSH_USER@$SSH_HOST "mkdir -p ~/webapps ~/sql"
scp -P $SSH_PORT $WAR_FILE $SSH_USER@$SSH_HOST:~/webapps/
scp -P $SSH_PORT database-functions.sql test-data.sql $SSH_USER@$SSH_HOST:~/sql/ 2>/dev/null || true

ssh -p $SSH_PORT $SSH_USER@$SSH_HOST << ENDSSH
pkill -9 -f wildfly 2>/dev/null || true
sleep 5

cp ~/webapps/person-management-system.war ~/wildfly-26.1.3/standalone/deployments/

cd ~/wildfly-26.1.3
unset _JAVA_OPTIONS

JAVA_OPTS="-Xms512m -Xmx1024m -XX:MetaspaceSize=384m -XX:MaxMetaspaceSize=768m --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED" \\
./bin/standalone.sh -b 0.0.0.0 -bmanagement=0.0.0.0 -Djboss.http.port=$WILDFLY_PORT > ~/wildfly.log 2>&1 &

sleep 40

curl -s http://localhost:$WILDFLY_PORT/person-management-system/api/persons > /dev/null
if [ \$? -eq 0 ]; then
    echo "API: OK on port $WILDFLY_PORT"
fi
ENDSSH

echo "Deploy complete"
