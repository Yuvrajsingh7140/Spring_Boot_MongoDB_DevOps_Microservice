#!/bin/bash
set -e

echo "🚀 Deploying Spring Boot MongoDB application locally with Docker Compose..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if .env file exists
if [ ! -f "deploy/.env" ]; then
    print_error ".env file not found. Please run ./scripts/setup.sh first."
    exit 1
fi

# Load environment variables
source deploy/.env

print_status "Starting local deployment..."

# Build and start services
cd deploy
docker-compose down --remove-orphans
docker-compose build --no-cache
docker-compose up -d

print_status "Services started. Waiting for health checks..."

# Wait for services to be ready
sleep 30

# Check service health
check_service() {
    local service_name=$1
    local url=$2
    local max_retries=30
    local retry=0

    print_status "Checking $service_name health..."

    while [ $retry -lt $max_retries ]; do
        if curl -f -s "$url" > /dev/null; then
            print_status "$service_name is healthy"
            return 0
        else
            echo "Waiting for $service_name... ($((retry + 1))/$max_retries)"
            sleep 5
            retry=$((retry + 1))
        fi
    done

    print_error "$service_name failed to start"
    return 1
}

# Check all services
check_service "Spring Boot App" "http://localhost:8080/actuator/health"
check_service "MongoDB" "http://localhost:27017"
check_service "Prometheus" "http://localhost:9090/-/ready"
check_service "Grafana" "http://localhost:3000/api/health"
check_service "Elasticsearch" "http://localhost:9200/_cluster/health"
check_service "Kibana" "http://localhost:5601/api/status"

cd ..

echo ""
echo "======================================"
echo "🎉 Local deployment completed successfully!"
echo "======================================"
echo ""
echo "Services available at:"
echo "📱 Spring Boot App:    http://localhost:8080"
echo "📊 Swagger UI:         http://localhost:8080/swagger-ui.html"
echo "🔍 Actuator:          http://localhost:8080/actuator"
echo "🗄️  MongoDB:           localhost:27017"
echo "📈 Prometheus:         http://localhost:9090"
echo "📊 Grafana:           http://localhost:3000 (admin/admin123)"
echo "🔍 Elasticsearch:     http://localhost:9200"
echo "📋 Kibana:            http://localhost:5601"
echo ""
echo "Useful commands:"
echo "• View logs:           docker-compose -f deploy/docker-compose.yml logs -f"
echo "• Stop services:       docker-compose -f deploy/docker-compose.yml down"
echo "• Restart app:         docker-compose -f deploy/docker-compose.yml restart app"
echo ""