#!/bin/bash
set -e

echo "🚀 Setting up Spring Boot MongoDB DevOps Project..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    # Check kubectl (optional for local development)
    if ! command -v kubectl &> /dev/null; then
        print_warning "kubectl is not installed. Install it for Kubernetes deployment."
    fi

    # Check Helm (optional for local development)
    if ! command -v helm &> /dev/null; then
        print_warning "Helm is not installed. Install it for Kubernetes deployment."
    fi

    # Check AWS CLI (optional for AWS deployment)
    if ! command -v aws &> /dev/null; then
        print_warning "AWS CLI is not installed. Install it for AWS deployment."
    fi

    # Check Terraform (optional for infrastructure deployment)
    if ! command -v terraform &> /dev/null; then
        print_warning "Terraform is not installed. Install it for infrastructure deployment."
    fi

    print_status "Prerequisites check completed."
}

# Initialize environment
initialize_environment() {
    print_status "Initializing environment..."

    # Create .env file from template if it doesn't exist
    if [ ! -f "deploy/.env" ]; then
        cp deploy/.env.template deploy/.env
        print_status "Created .env file from template. Please update it with your configuration."
    fi

    # Create necessary directories
    mkdir -p logs
    mkdir -p data/mongodb
    mkdir -p data/prometheus
    mkdir -p data/grafana
    mkdir -p data/elasticsearch

    print_status "Environment initialized."
}

# Build application
build_application() {
    print_status "Building Spring Boot application..."

    cd app
    mvn clean package -DskipTests
    cd ..

    print_status "Application built successfully."
}

# Setup Git repository
setup_git() {
    print_status "Setting up Git repository..."

    if [ ! -d ".git" ]; then
        git init
        git add .
        git commit -m "Initial commit: DevOps automation project setup"
        print_status "Git repository initialized with initial commit."
    else
        print_warning "Git repository already exists."
    fi
}

# Main execution
main() {
    echo "======================================"
    echo "Spring Boot MongoDB DevOps Setup"
    echo "======================================"

    check_prerequisites
    initialize_environment
    build_application
    setup_git

    echo ""
    print_status "Setup completed successfully! 🎉"
    echo ""
    echo "Next steps:"
    echo "1. Update deploy/.env with your configuration"
    echo "2. Run './scripts/deploy-local.sh' for local development"
    echo "3. Run './scripts/deploy-k8s.sh' for Kubernetes deployment"
    echo "4. Check README.md for detailed instructions"
}

# Run main function
main "$@"