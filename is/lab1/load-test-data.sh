#!/bin/bash

set -e

if [ ! -f .env ]; then
    echo "Ошибка: файл .env не найден"
    exit 1
fi

source .env

SSH_PORT=${SSH_PORT:-2222}

echo "=== Load test data ==="

scp -P $SSH_PORT test-data.sql $SSH_USER@$SSH_HOST:~/sql/

ssh -p $SSH_PORT $SSH_USER@$SSH_HOST << 'ENDSSH'
psql -h pg -d studs -f ~/sql/test-data.sql
ENDSSH

echo "Test data loaded"

