# 🚀 Flagship E-Commerce Microservices Suite

A comprehensive, enterprise-level e-commerce microservices suite demonstrating advanced Java engineering skills, clean architecture principles, and modern technologies used by top-tier companies.

## 🏗️ Architecture Overview

### Microservices
- **API Gateway** (Port 8081) - Spring Cloud Gateway for routing, auth, rate limiting
- **User Service** (Port 8082) - User management with Keycloak integration
- **Order Service** (Port 8083) - Order processing with domain events
- **Payment Service** (Port 8084) - Payment processing with Resilience4j
- **Inventory Service** (Port 8085) - Stock management with Redis caching
- **Streaming Service** (Port 8086) - Real-time events with WebFlux & SSE

### Infrastructure
- **PostgreSQL** (Port 5432) - Database for all services
- **Redis** (Port 6379) - Distributed caching
- **Kafka** (Port 9092) - Event streaming platform
- **Keycloak** (Port 8080) - Identity and Access Management
- **Prometheus** (Port 9090) - Metrics collection
- **Grafana** (Port 3000) - Metrics visualization

## 🛠️ Technology Stack

### Core Technologies
- **Spring Boot 3.2** with Java 17
- **Spring Cloud Gateway** for API routing
- **Spring Security** with OAuth2 Resource Server
- **Spring Data JPA** with PostgreSQL
- **Spring WebFlux** for reactive programming
- **Apache Kafka** for event-driven communication
- **Redis** for distributed caching
- **Resilience4j** for fault tolerance
- **Keycloak** for identity management
- **Prometheus & Grafana** for observability

### Infrastructure
- **Docker** containerization
- **Kubernetes** orchestration
- **GitHub Actions** CI/CD pipeline
- **Maven** build automation

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.9+
- Docker & Docker Compose
- Git

### Running the Application

#### Option 1: Using Docker Compose (Recommended)
```bash
# Start infrastructure services
docker-compose up -d postgres redis kafka zookeeper keycloak prometheus grafana

# Wait for infrastructure to be ready
sleep 30

# Start microservices (in separate terminals)
cd services/api-gateway && mvn spring-boot:run -Dspring-boot.run.profiles=docker
cd services/user-service && mvn spring-boot:run -Dspring-boot.run.profiles=docker
cd services/order-service && mvn spring-boot:run -Dspring-boot.run.profiles=docker
cd services/payment-service && mvn spring-boot:run -Dspring-boot.run.profiles=docker
cd services/inventory-service && mvn spring-boot:run -Dspring-boot.run.profiles=docker
cd services/streaming-service && mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

#### Option 2: Using the Run Script
```bash
./run-services.sh
```

#### Option 3: Using Kubernetes
```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n flagship
```

## 📊 Service Endpoints

### API Gateway
- **Health Check**: http://localhost:8081/actuator/health
- **Metrics**: http://localhost:8081/actuator/prometheus

### User Service
- **Health Check**: http://localhost:8082/actuator/health
- **Users API**: http://localhost:8082/api/users

### Order Service
- **Health Check**: http://localhost:8083/actuator/health
- **Orders API**: http://localhost:8083/api/orders

### Payment Service
- **Health Check**: http://localhost:8084/actuator/health
- **Payments API**: http://localhost:8084/api/payments

### Inventory Service
- **Health Check**: http://localhost:8085/actuator/health
- **Inventory API**: http://localhost:8085/api/inventory

### Streaming Service
- **Health Check**: http://localhost:8086/actuator/health
- **SSE Stream**: http://localhost:8086/api/stream/events

### Monitoring
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Keycloak**: http://localhost:8080 (admin/admin)

## 🏛️ Architecture Features

### Cross-Cutting Concerns
- ✅ **Security**: Keycloak + JWT validation
- ✅ **Messaging**: Kafka event-driven architecture
- ✅ **Resilience**: Circuit breakers, rate limiting, retry logic
- ✅ **Caching**: Redis distributed caching
- ✅ **Observability**: Prometheus metrics + Grafana dashboards
- ✅ **CI/CD**: Automated testing, building, and deployment

### Enterprise Patterns
- ✅ **Domain-Driven Design** with bounded contexts
- ✅ **Event Sourcing** for critical business events
- ✅ **CQRS** patterns where appropriate
- ✅ **Circuit Breaker** pattern for fault tolerance
- ✅ **Idempotency** for payment processing
- ✅ **Health Checks** and monitoring

## 📁 Project Structure

```
/springboot-flagship/
├── README.md
├── pom.xml (Parent Maven project)
├── docker-compose.yml
├── run-services.sh
├── .github/workflows/ci-cd.yml
├── k8s/ (Kubernetes manifests)
├── docs/ (Comprehensive documentation)
│   ├── ARCHITECTURE.md
│   ├── CASE_STUDY_ORDER_SCALING.md
│   └── diagrams/ (PlantUML diagrams)
└── services/ (6 microservices)
    ├── api-gateway/
    ├── user-service/
    ├── order-service/
    ├── payment-service/
    ├── inventory-service/
    └── streaming-service/
```

## 🔧 Development

### Building the Project
```bash
# Build all services
mvn clean package -DskipTests

# Build individual service
cd services/api-gateway
mvn clean package -DskipTests
```

### Running Tests
```bash
# Run all tests
mvn test

# Run tests for specific service
cd services/api-gateway
mvn test
```

### Code Quality
- **Checkstyle**: Code style enforcement
- **SpotBugs**: Static analysis
- **JaCoCo**: Code coverage
- **Lombok**: Boilerplate reduction

## 📈 Monitoring & Observability

### Metrics
- **Application Metrics**: Custom business metrics
- **JVM Metrics**: Memory, GC, threads
- **HTTP Metrics**: Request/response times
- **Database Metrics**: Connection pools, query times

### Health Checks
- **Liveness Probes**: Service availability
- **Readiness Probes**: Service readiness
- **Custom Health Indicators**: Business logic health

### Logging
- **Structured Logging**: JSON format
- **Log Aggregation**: Centralized logging
- **Correlation IDs**: Request tracing

## 🔒 Security

### Authentication & Authorization
- **Keycloak**: Centralized identity management
- **JWT**: Stateless authentication tokens
- **OAuth2**: Authorization framework
- **RBAC**: Role-based access control

### Network Security
- **TLS**: Encrypted communication
- **Service Mesh**: mTLS between services
- **Network Policies**: Kubernetes network isolation

## 🚀 Deployment

### Docker
```bash
# Build Docker images
docker-compose build

# Run with Docker Compose
docker-compose up -d
```

### Kubernetes
```bash
# Deploy to Kubernetes
kubectl apply -f k8s/

# Scale services
kubectl scale deployment api-gateway --replicas=3 -n flagship
```

### CI/CD
- **GitHub Actions**: Automated pipeline
- **Multi-stage Pipeline**: Test, build, deploy
- **Security Scanning**: Trivy vulnerability scanning
- **Environment Promotion**: Staging to production

## 📚 Documentation

- **[Architecture Guide](docs/ARCHITECTURE_PLAN.md)** - Detailed system design
- **[Case Study](docs/SCALING_PLAN.md)** - Order processing at scale
- **[Architecture Diagrams](docs/diagrams/)** - PlantUML diagrams

## 🏆 What This Demonstrates

This project showcases:
- **Advanced Java Skills** - Spring Boot, Spring Cloud, reactive programming
- **Microservices Architecture** - Service decomposition, event-driven design
- **Cloud-Native Technologies** - Kubernetes, Docker, observability
- **Enterprise Patterns** - DDD, CQRS, Event Sourcing, Circuit Breaker
- **DevOps Practices** - CI/CD, infrastructure as code, monitoring
- **Clean Architecture** - SOLID principles, separation of concerns
- **Performance Engineering** - Caching, async processing, scaling strategies

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Apache Kafka for event streaming
- Keycloak for identity management
- Prometheus & Grafana for observability
- The open-source community

---

**Built with ❤️ by Marios Gavriil demonstrating enterprise-level skills**