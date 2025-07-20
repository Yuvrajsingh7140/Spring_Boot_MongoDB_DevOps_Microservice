#!/bin/bash
set -e

echo "🚀 Deploying to Kubernetes..."

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

# Check kubectl
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed. Please install kubectl first."
    exit 1
fi

# Check connection to cluster
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please configure kubectl."
    exit 1
fi

NAMESPACE="devops-microservice"

# Create namespace
print_status "Creating namespace: $NAMESPACE"
kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -

# Apply Kubernetes manifests
print_status "Applying Kubernetes manifests..."

kubectl apply -f deploy/k8s/namespace.yaml
kubectl apply -f deploy/k8s/configmap.yaml
kubectl apply -f deploy/k8s/secrets.yaml
kubectl apply -f deploy/k8s/rbac.yaml
kubectl apply -f deploy/k8s/mongodb.yaml
kubectl apply -f deploy/k8s/deployment.yaml
kubectl apply -f deploy/k8s/service.yaml
kubectl apply -f deploy/k8s/ingress.yaml
kubectl apply -f deploy/k8s/hpa.yaml
kubectl apply -f deploy/k8s/pdb.yaml
kubectl apply -f deploy/k8s/network-policies.yaml

print_status "Waiting for deployments to be ready..."

# Wait for MongoDB
kubectl wait --for=condition=available --timeout=300s deployment/mongodb -n $NAMESPACE

# Wait for application
kubectl wait --for=condition=available --timeout=300s deployment/springboot-mongodb-app -n $NAMESPACE

print_status "Checking pod status..."
kubectl get pods -n $NAMESPACE

print_status "Checking service status..."
kubectl get svc -n $NAMESPACE

# Get service URL
SERVICE_URL=$(kubectl get svc springboot-mongodb-app-service -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")

echo ""
echo "======================================"
echo "🎉 Kubernetes deployment completed!"
echo "======================================"
echo ""
echo "Deployment status:"
kubectl get deployment -n $NAMESPACE
echo ""
echo "Service endpoint: $SERVICE_URL"
echo ""
echo "Useful commands:"
echo "• Check pods:          kubectl get pods -n $NAMESPACE"
echo "• Check logs:          kubectl logs -f deployment/springboot-mongodb-app -n $NAMESPACE"
echo "• Port forward:        kubectl port-forward svc/springboot-mongodb-app-service 8080:80 -n $NAMESPACE"
echo "• Scale deployment:    kubectl scale deployment springboot-mongodb-app --replicas=5 -n $NAMESPACE"
echo ""