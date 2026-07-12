#!/bin/bash

echo "===== MinIO Setup Script ====="
echo "This script initializes MinIO for the Person Management System"
echo ""

MINIO_ENDPOINT=${MINIO_ENDPOINT:-"http://localhost:9000"}
MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY:-"minioadmin"}
MINIO_SECRET_KEY=${MINIO_SECRET_KEY:-"minioadmin"}
BUCKET_NAME=${MINIO_BUCKET:-"person-imports"}

echo "MinIO Endpoint: $MINIO_ENDPOINT"
echo "Bucket Name: $BUCKET_NAME"
echo ""

if ! command -v mc &> /dev/null; then
    echo "ERROR: MinIO client (mc) is not installed"
    echo "Please install it: https://min.io/docs/minio/linux/reference/minio-mc.html"
    exit 1
fi

echo "Step 1: Configuring MinIO alias..."
mc alias set local $MINIO_ENDPOINT $MINIO_ACCESS_KEY $MINIO_SECRET_KEY

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to connect to MinIO"
    echo "Make sure MinIO is running at $MINIO_ENDPOINT"
    exit 1
fi

echo "Step 2: Creating bucket '$BUCKET_NAME'..."
mc mb local/$BUCKET_NAME --ignore-existing

echo "Step 3: Setting anonymous download policy..."
mc anonymous set download local/$BUCKET_NAME

echo "Step 4: Checking bucket status..."
mc ls local/

echo ""
echo "===== MinIO Setup Complete ====="
echo "Bucket '$BUCKET_NAME' is ready for file uploads"
echo "You can access MinIO Console at: http://localhost:9001"
echo "Username: $MINIO_ACCESS_KEY"
echo "Password: $MINIO_SECRET_KEY"
