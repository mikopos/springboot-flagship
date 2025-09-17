# Spring Boot Microservices Flagship Project

## 🚀 Project Overview

This is a comprehensive **microservices architecture** project built with **Spring Boot 3.x** and **Java 21**, demonstrating modern enterprise application development practices. The project showcases a complete e-commerce platform with multiple interconnected services, implementing industry-standard patterns and technologies.

## 🎯 Purpose & Learning Objectives

This project demonstrates proficiency in:

- **Microservices Architecture** - Service decomposition and communication
- **Spring Boot 3.x** - Modern Spring framework features
- **Java 21** - Latest Java features and performance improvements
- **Docker & Docker Compose** - Containerization and orchestration
- **Spring Security** - OAuth2/JWT authentication and authorization
- **Spring Cloud Gateway** - API Gateway pattern implementation
- **Event-Driven Architecture** - Asynchronous communication with Kafka
- **Database Design** - PostgreSQL with JPA/Hibernate
- **Caching** - Redis integration for performance optimization
- **Monitoring** - Prometheus and Grafana integration
- **CI/CD** - GitHub Actions for automated testing and deployment
- **Security** - Vulnerability scanning and dependency management

## 🏗️ Architecture

The project follows a **microservices architecture** with the following services:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │   User Service  │    │  Order Service  │
│   (Port 8081)   │    │   (Port 8082)   │    │   (Port 8083)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│Payment Service  │    │Inventory Service│    │Streaming Service│
│  (Port 8084)    │    │  (Port 8085)    │    │  (Port 8086)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### Service Responsibilities

- **API Gateway**: Central entry point, routing, authentication, rate limiting
- **User Service**: User management, authentication, profile management
- **Order Service**: Order processing, order history, order status tracking
- **Payment Service**: Payment processing, transaction management, refunds
- **Inventory Service**: Product catalog, stock management, inventory tracking
- **Streaming Service**: Real-time data streaming, event processing

## 🛠️ Technologies Used

### Backend Technologies
- **Java 21** - Latest LTS version with modern features
- **Spring Boot 3.5.5** - Latest stable version
- **Spring Cloud Gateway** - API Gateway implementation
- **Spring Security** - OAuth2/JWT authentication
- **Spring Data JPA** - Database abstraction layer
- **Spring Data Redis** - Caching and session management
- **Spring Kafka** - Event-driven messaging
- **Hibernate** - ORM framework
- **Maven** - Build and dependency management

### Databases & Caching
- **PostgreSQL 15** - Primary relational database
- **Redis 7** - In-memory caching and session store
- **Apache Kafka** - Event streaming platform

### Infrastructure & DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration
- **Prometheus** - Metrics collection and monitoring
- **Grafana** - Metrics visualization and dashboards
- **GitHub Actions** - CI/CD pipeline
- **Trivy** - Container vulnerability scanning

### Security & Quality
- **OWASP Dependency Check** - Vulnerability scanning
- **JWT** - Stateless authentication
- **OAuth2** - Authorization framework
- **Keycloak** - Identity and access management

## 📋 Prerequisites

Before running this project, ensure you have the following installed:

- **Java 21**
- **Docker** and **Docker Compose**
- **Maven 3.9+**
- **Git**

## 🚀 Quick Start Guide

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd springboot-flagship
   ```

2. **Start all services with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Wait for services to start** (approximately 2-3 minutes)
   ```bash
   # Check service status
   docker-compose ps
   
   # View logs
   docker-compose logs -f
   ```

4. **Access the application**
   - **API Gateway**: http://localhost:8081
   - **Keycloak Admin**: http://localhost:8080 (admin/admin)
   - **Grafana**: http://localhost:3000 (admin/admin)
   - **Prometheus**: http://localhost:9090

### Option 2: Local Development

1. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis kafka zookeeper keycloak
   ```

2. **Build and run services locally**
   ```bash
   # Build all services
   mvn clean package -DskipTests
   
   # Run each service (in separate terminals)
   java -jar services/api-gateway/target/api-gateway-1.0.0.jar
   java -jar services/user-service/target/user-service-1.0.0.jar
   java -jar services/order-service/target/order-service-1.0.0.jar
   java -jar services/payment-service/target/payment-service-1.0.0.jar
   java -jar services/inventory-service/target/inventory-service-1.0.0.jar
   java -jar services/streaming-service/target/streaming-service-1.0.0.jar
   ```

## 🔧 Configuration

### Environment Variables

The application uses the following key environment variables:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/flagship_db
SPRING_DATASOURCE_USERNAME=flagship_user
SPRING_DATASOURCE_PASSWORD=flagship_password

# Redis Configuration
SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379

# Kafka Configuration
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092

# Keycloak Configuration
KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/flagship
KEYCLOAK_JWK_SET_URI=http://keycloak:8080/realms/flagship/protocol/openid-connect/certs
```

### Profiles

The application supports multiple profiles:
- **default** - Local development
- **docker** - Docker containerized environment
- **test** - Testing environment

## 🧪 Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run tests for specific service
mvn test -pl services/user-service

# Run with Docker
docker-compose -f docker-compose.test.yml up --abort-on-container-exit
```

### API Testing

#### Getting JWT Token

1. **Access Keycloak Admin Console**
   - URL: http://localhost:8080
   - Username: `admin`
   - Password: `admin`

2. **Create a test user** in Keycloak admin console

3. **Get JWT token using the helper script:**
   ```bash
   # Make script executable (if not already)
   chmod +x scripts/get-token.sh
   
   # Get token (replace with your credentials)
   ./scripts/get-token.sh your-username your-password
   ```

4. **Or get token manually:**
   ```bash
   curl -X POST http://localhost:8080/realms/flagship/protocol/openid-connect/token \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "username=your-username" \
     -d "password=your-password" \
     -d "grant_type=password" \
     -d "client_id=flagship-client"
   ```

#### Testing API Endpoints

```bash
# Health check (no auth required)
curl http://localhost:8081/actuator/health

# Test all endpoints with JWT token
./scripts/test-api.sh

# Or test individual endpoints
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/inventory-service/products

curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/user-service/users

curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8081/order-service/orders
```

## 📊 Monitoring & Observability

### Prometheus Metrics
- **Endpoint**: http://localhost:9090
- **Metrics**: JVM, HTTP requests, database connections, custom business metrics

### Grafana Dashboards
- **Endpoint**: http://localhost:3000
- **Login**: admin/admin
- **Dashboards**: Microservices overview, JVM metrics, business metrics

### Application Health
- **Health Checks**: http://localhost:8081/actuator/health
- **Metrics**: http://localhost:8081/actuator/metrics
- **Info**: http://localhost:8081/actuator/info

## 🔒 Security Features

- **JWT Authentication** - Stateless token-based authentication
- **OAuth2 Authorization** - Role-based access control
- **API Gateway Security** - Centralized security policies
- **Dependency Scanning** - Automated vulnerability detection
- **Container Security** - Trivy scanning in CI/CD pipeline

## 🚀 CI/CD Pipeline

The project includes a comprehensive GitHub Actions workflow:

1. **Code Quality Checks**
   - Maven compilation
   - Unit tests execution
   - Code coverage reporting

2. **Security Scanning**
   - OWASP dependency check
   - Trivy container scanning
   - SARIF report generation

3. **Build & Deploy**
   - Docker image building
   - Container registry push
   - Automated deployment

## 📁 Project Structure

```
springboot-flagship/
├── services/                    # Microservices
│   ├── api-gateway/            # API Gateway service
│   ├── user-service/           # User management service
│   ├── order-service/          # Order processing service
│   ├── payment-service/        # Payment processing service
│   ├── inventory-service/      # Inventory management service
│   └── streaming-service/      # Event streaming service
├── k8s/                        # Kubernetes deployment files
├── monitoring/                 # Prometheus & Grafana configs
├── docs/                       # Documentation
├── docker-compose.yml          # Docker Compose configuration
├── pom.xml                     # Parent Maven POM
└── README.md                   # This file
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 8080-8086, 5432, 6379, 9092, 9090, 3000 are available
2. **Memory issues**: Increase Docker memory allocation to at least 4GB
3. **Service startup**: Wait for all services to be healthy before testing

### Useful Commands

```bash
# Check service health
docker-compose ps

# View service logs
docker-compose logs -f <service-name>

# Restart a service
docker-compose restart <service-name>

# Clean up everything
docker-compose down -v
docker system prune -a
```

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/your-repo/issues) page
2. Create a new issue with detailed information
3. Include logs and error messages

---

**Built with ❤️ using Spring Boot, Java 21, and modern microservices patterns**
