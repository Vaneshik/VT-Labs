#!/bin/bash

if [ ! -f .env ]; then
    echo "ERROR: .env file not found"
    exit 1
fi

source .env

SSH_PORT=${SSH_PORT:-2222}
SSH_USER=${SSH_USER:-s409858}
SSH_HOST=${SSH_HOST:-se.ifmo.ru}

echo "Killing MinIO on Helios..."
ssh -p $SSH_PORT $SSH_USER@$SSH_HOST 'pkill -9 -f "minio server" 2>/dev/null || true; sleep 2'

ssh -p $SSH_PORT $SSH_USER@$SSH_HOST << 'ENDSSH'
if ps aux | grep -v grep | grep "minio server" > /dev/null; then
    echo "ERROR: MinIO still running"
    exit 1
else
    echo "MinIO killed"
fi
ENDSSH
