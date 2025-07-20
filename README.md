# Spring Boot MongoDB DevOps Microservice

This project is a **complete DevOps automation solution** for a Spring Boot microservice backed by MongoDB. It includes:

* **Spring Boot** REST API with `DevOps Team`
* **MongoDB** database integration
* **Docker** containerization
* **Terraform** infrastructure-as-code for AWS (EKS, VPC, ECR, S3, IAM, etc.)
* **Helm** charts and **Kubernetes** manifests for container orchestration
* **Jenkins** CI/CD pipeline (Declarative Pipeline) with Docker-in-Docker, Trivy, SonarQube, etc.
* **Prometheus + Grafana** monitoring stack
* **ELK** (Elasticsearch, Logstash, Kibana) logging stack
* **Scripts** for local and Kubernetes deployment
* **Unit and integration tests** with JUnit 5 and TestContainers

## Quick Start

### Local Development

```bash
# Run setup (creates .env, build application, etc.)
./scripts/setup.sh

# Start local stack with Docker Compose
./scripts/deploy-local.sh
```

### Kubernetes Deployment

```bash
# Update kubeconfig for your EKS cluster
aws eks update-kubeconfig --region us-west-2 --name devops-eks-cluster

# Deploy manifests
./scripts/deploy-k8s.sh
```

### Terraform Infrastructure

```bash
cd deploy/terraform
terraform init
terraform plan
terraform apply
```

## Directory Structure

```text
springboot-mongodb-devops/
├── app/                     # Spring Boot source code
├── deploy/                  # Deployment configuration (Docker, K8s, Terraform, Jenkins, Helm)
├── monitoring/              # Prometheus & Grafana configs
├── logging/                 # ELK stack configs
├── scripts/                 # Utility scripts
├── docs/                    # Documentation (optional)
└── README.md
```

## CI/CD Pipeline (Jenkins)

The Declarative Pipeline performs the following steps:

1. **Checkout** source code
2. **Build & Test** (Maven, Unit tests)
3. **Security Scan** (OWASP Dependency Check, Trivy)
4. **Code Quality** (SonarQube)
5. **Docker Build & Push** to Amazon ECR
6. **Deploy** to EKS & Run Smoke Tests
7. **Rollback** on failure
8. **Notify** via Slack

## Monitoring & Observability

* **Prometheus** collects JVM, HTTP, MongoDB, and Kubernetes cluster metrics.
* **Grafana** dashboards visualize application performance and infrastructure health.
* **ELK Stack** aggregates JSON logs with correlation IDs for distributed tracing.

## Security & Best Practices

* Runs as **non-root** user in containers.
* Uses **AWS Parameter Store** / Kubernetes **Secrets** for sensitive configuration.
* **Network Policies**, **RBAC**, **Pod Security Contexts**, and **Resource** constraints to enhance cluster security.
* **Container image scanning** with Trivy and Dependency scanning with OWASP.

## Contributing

Feel free to open issues or pull requests if you have improvements!

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
