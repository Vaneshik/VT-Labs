#!/bin/bash

# Скрипт для загрузки тестовых данных в PostgreSQL на Helios

source .env

echo "===== Загрузка тестовых данных в PostgreSQL ====="
echo ""

# Проверка наличия test-data.sql
if [ ! -f "test-data.sql" ]; then
    echo "❌ Файл test-data.sql не найден"
    exit 1
fi

echo "📤 Загружаем test-data.sql на Helios..."
scp -P $SSH_PORT test-data.sql $SSH_USER@$SSH_HOST:~/test-data.sql

if [ $? -ne 0 ]; then
    echo "❌ Ошибка при загрузке файла на Helios"
    exit 1
fi

echo "📊 Выполняем SQL скрипт в PostgreSQL..."
ssh -p $SSH_PORT $SSH_USER@$SSH_HOST << ENDSSH
psql -h pg -d $DB_NAME -U $DB_USER -f ~/test-data.sql
if [ \$? -eq 0 ]; then
    echo "✅ Тестовые данные успешно загружены!"
else
    echo "❌ Ошибка при выполнении SQL скрипта"
    exit 1
fi

# Удаляем временный файл
rm -f ~/test-data.sql
ENDSSH

echo ""
