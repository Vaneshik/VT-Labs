#!/bin/bash

echo "======================================"
echo "  Person Management System - Lab 3   "
echo "======================================"
echo ""

PROJECT_DIR=$(pwd)

echo "Step 1: Checking prerequisites..."
echo ""

if ! command -v docker &> /dev/null; then
    echo "❌ ERROR: Docker is not installed"
    echo "Please install Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ ERROR: docker-compose is not installed"
    echo "Please install docker-compose: https://docs.docker.com/compose/install/"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ ERROR: Maven is not installed"
    echo "Please install Maven: https://maven.apache.org/install.html"
    exit 1
fi

echo "✅ All prerequisites are installed"
echo ""

echo "Step 2: Starting infrastructure (PostgreSQL + MinIO)..."
docker-compose up -d

echo "Waiting for services to start..."
sleep 10

echo ""
echo "Step 3: Initializing MinIO bucket..."
if [ -f "./setup-minio.sh" ]; then
    chmod +x ./setup-minio.sh
    ./setup-minio.sh
else
    echo "⚠️  setup-minio.sh not found, skipping MinIO initialization"
fi

echo ""
echo "Step 4: Building application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo ""
echo "======================================"
echo "  Setup Complete!                    "
echo "======================================"
echo ""
echo "Next steps:"
echo ""
echo "1. Deploy backend WAR file:"
echo "   cp target/person-management-system.war /path/to/wildfly/deployments/"
echo ""
echo "2. Start frontend:"
echo "   cd frontend"
echo "   npm install"
echo "   npm start"
echo ""
echo "3. Access application:"
echo "   Frontend: http://localhost:3000"
echo "   Backend API: http://localhost:8080/person-management-system/api"
echo "   MinIO Console: http://localhost:9001"
echo ""
echo "4. Test failure scenarios:"
echo "   ./test-failure-scenarios.sh"
echo ""
echo "5. View cache statistics:"
echo "   curl http://localhost:8080/person-management-system/api/cache/stats"
echo ""
echo "Services running:"
docker-compose ps
echo ""
echo "To stop services: docker-compose down"
echo "======================================"
