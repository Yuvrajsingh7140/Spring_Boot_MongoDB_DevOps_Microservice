variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-west-2"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "springboot-mongodb-devops"
}

variable "cluster_name" {
  description = "EKS cluster name"
  type        = string
  default     = "devops-eks-cluster"
}

variable "k8s_version" {
  description = "Kubernetes version"
  type        = string
  default     = "1.28"
}

variable "vpc_cidr" {
  description = "CIDR block for VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones_count" {
  description = "Number of availability zones"
  type        = number
  default     = 3
}

variable "ecr_repository_name" {
  description = "ECR repository name"
  type        = string
  default     = "springboot-mongodb-app"
}

variable "mongo_username" {
  description = "MongoDB username"
  type        = string
  default     = "admin"
  sensitive   = true
}

variable "mongo_password" {
  description = "MongoDB password"
  type        = string
  default     = "password123"
  sensitive   = true
}

variable "jenkins_instance_type" {
  description = "EC2 instance type for Jenkins"
  type        = string
  default     = "t3.medium"
}