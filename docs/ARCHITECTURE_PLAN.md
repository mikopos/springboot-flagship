# Flagship E-Commerce Microservices Architecture

## What is Flagship?

Springboot-flagship is my take on building a modern e-commerce platform using microservices.
I wanted to showcase what I've learned about enterprise Java development, clean architecture
and the technologies that top tier companies actually use in production.

## My Design Philosophy

### 1. Microservices Done Right

I've broken down the system into focused services, where each one owns its specific domain and data.
The key here is keeping services loosely coupled and talk to each other through well-defined APIs
and events, without the need to know about each other's internal workings. Each service has one
clear job and can be deployed independently.

### 2. Domain-Driven Design

Following DDD principles, each service represents a distinct business domain. When something
important happens in one service, it publishes events that other services can react to. This keeps
the system flexible and makes it easier to add new features without breaking existing ones.

### 3. Event-Driven Communication

Instead of services calling each other directly, they communicate through <b>Kafka events</b>. This makes
the system more resilient and allows a better scalability. I'm also storing critical business
events for audit trails and potential replay scenarios.

## The Services I Built

### API Gateway (`api-gateway`)

**Port**: 8081  
**Built with**: Spring Cloud Gateway, WebFlux

This is the front door to my entire system. It handles all the incoming requests and routes them to
the appropriate services. I've implemented **OAuth2** with JWT validation, so users get authenticated
once and can access all services. The gateway also includes **circuit breakers** to prevent cascade
failures and **rate limiting** to protect against abuse.

**What it does**:

- Routes requests to the right service
- Validates JWT tokens from Keycloak
- Implements circuit breakers and rate limiting
- Logs all requests for monitoring
- Provides health check endpoints

### User Service (`user-service`)

**Port**: 8082  
**Built with**: Spring Boot, Spring Data JPA, PostgreSQL

This service manages everything about users, their profiles, preferences, and activity history.
I've integrated it with **Keycloak** for authentication, so it works seamlessly with the **OAuth2** flow.
The service tracks user activity for analytics and provides a clean API for user management.

**What it does**:

- Manages user profiles and preferences
- Tracks user activity for analytics
- Integrates with Keycloak for authentication
- Provides user management APIs

### Order Service (`order-service`)

**Port**: 8083  
**Built with**: Spring Boot, Spring Data JPA, PostgreSQL, Kafka

This is where the magic happens. This is the service about order creation, management and tracking. 
When an order is created, it publishes events that trigger payment processing and inventory updates. 
I've made sure to maintain **transactional consistency** while keeping the system responsive.

**What it does**:

- Creates and manages orders
- Handles order state transitions
- Publishes order events to Kafka
- Maintains order history and tracking

### Payment Service (`payment-service`)

**Port**: 8084  
**Built with**: Spring Boot, Spring Data JPA, PostgreSQL, Kafka, Resilience4j

Payment processing is critical, so I've made this service extra resilient. It uses **Resilience4j** for
**circuit breakers, retries and rate limiting**. I've also implemented **idempotency** to handle duplicate
payment requests gracefully. The service integrates with **external payment providers** while
maintaining data consistency.

**What it does**:

- Processes payments through external providers
- Handles payment state management
- Implements idempotency for duplicate requests
- Uses circuit breakers for external calls

### Inventory Service (`inventory-service`)

**Port**: 8085  
**Built with**: Spring Boot, Spring Data JPA, PostgreSQL, Redis, Kafka

This service manages the product catalog and inventory levels. I've added **Redis caching** to make
product lookups super fast and it updates inventory in real-time when orders are placed. The
service publishes events when stock levels change, so other services can react accordingly.

**What it does**:

- Manages product catalog
- Tracks inventory levels
- Uses Redis for fast product lookups
- Publishes inventory events

### Streaming Service (`streaming-service`)

**Port**: 8086  
**Built with**: Spring WebFlux, Kafka

This service provides real-time updates to the frontend using **Server-Sent Events**. It's built with
**WebFlux** for reactive programming, so it can handle many concurrent connections efficiently. The
service filters and routes events to the right clients.

**What it does**:

- Streams real-time events to clients
- Uses Server-Sent Events for live updates
- Filters and routes events appropriately
- Manages client connections efficiently

## The Infrastructure That Makes It All Work

### Security

I'm using Keycloak for identity and access management. It's a solid open-source solution that
handles OAuth2 and JWT tokens really well. **Each service validates JWT tokens independently**, so the
system stays stateless and scalable.

### Messaging

**Kafka** is the backbone of my **event-driven architecture**. When something important happens (like an
order being created), services publish events that other services can react to. I'm storing critical
business events for audit trails and potential replay scenarios.

### Resilience

I've implemented **Resilience4j** throughout the system for **circuit breakers, rate limiting, and retry
logic**. This prevents cascade failures and handles transient issues gracefully. The payment service
especially benefits from this since it talks to external providers.

### Observability

I'm using **Prometheus** for metrics collection and **Grafana** for visualization. Each service exposes
metrics through **Micrometer**, and **Spring Actuator** provides health checks. This gives me visibility
into how the system is performing.

### Caching

**Redis** provides **distributed caching** for the inventory service. I'm using the **cache-aside** pattern with
TTL, and cache invalidation happens through events when inventory changes.

## How I Handle Data

### Database per Service

I've given each service its own database to maintain data isolation:

- **User Service**: `flagship_users` database
- **Order Service**: `flagship_orders` database
- **Payment Service**: `flagship_payments` database
- **Inventory Service**: `flagship_inventory` database

This approach prevents services from stepping on each other's data and makes it easier to scale
individual services.

### Event Store

I'm storing important business events in dedicated tables:

- **Order Events**: `order_events` table
- **Payment Events**: `payment_events` table
- **Product Events**: `product_events` table

This gives me a complete audit trail and allows for potential replay scenarios if needed.

### Caching Strategy

Redis handles distributed caching for the inventory service. I'm using structured cache keys with
configurable TTL, and cache invalidation happens automatically when inventory events are published.

## Deployment and Operations

### Containerization

Everything runs in **Docker containers** with multi-stage builds to keep image sizes small. I'm using
OpenJDK 24 as the base image and each container includes health check endpoints.

### Kubernetes Orchestration

The entire system runs on Kubernetes with rolling updates, load balancing, and horizontal pod
autoscaling. This makes the system resilient and allows it to scale based on demand.

### CI/CD Pipeline

I've set up **GitHub Actions for automated testing, building, and deployment**. The pipeline includes
security scanning with Trivy and promotes changes from staging to production automatically.

## Monitoring and Observability

### Metrics

I'm collecting custom business metrics alongside standard JVM metrics (memory, GC, threads) and HTTP
metrics (request/response times). Database connection pools and query times are also monitored to
catch performance issues early.

### Logging

All services use structured JSON logging with correlation IDs for request tracing. Log levels are
configurable per environment and I'm planning to set up centralized log aggregation for better
debugging.

### Tracing

I'm working on implementing distributed tracing to track requests across services. This will help
identify bottlenecks and make error tracking much easier when things go wrong.

## Security

### Authentication

Keycloak handles centralized identity management with JWT tokens and OAuth2. I've implemented
role-based access control so different users can have different permissions.

### Authorization

Each service validates JWT tokens independently and implements method-level security. API endpoints
are protected, and sensitive data is handled carefully.

### Network Security

**All communication is encrypted with TLS** and I'm planning to implement **mTLS** between services.
Kubernetes network policies provide additional isolation.

## Performance and Reliability

### Scalability

The system scales horizontally using Kubernetes HPA and load balancing. Redis caching and database
connection pooling help with performance.

### Performance

I'm using async processing where possible, implementing connection pooling and caching frequently
accessed data. Response compression is also in place.

### Reliability

Circuit breakers prevent cascade failures, retry logic handles transient issues and health checks
monitor service status. The system is designed to degrade gracefully when individual services fail.

## Development Approach

### Code Quality

I've focused on writing clean, readable code following **SOLID** principles and **proven design patterns.**
The goal is to make the **codebase maintainable and easy for other developers to understand.**

### Testing

I'm implementing comprehensive testing at multiple levels:

- Unit tests for individual components (TBD)
- Integration tests for service interactions (TBD)
- Contract tests to ensure API compatibility (TBD)
- End-to-end tests for full system validation (TBD)
 
### Documentation

Each service includes OpenAPI/Swagger documentation, and I'm maintaining this architecture
documentation along with operational runbooks. Code comments explain complex business logic.

## What's Next

### Planned Features

I'm planning to add:

- Service mesh integration with Istio
- Complete event sourcing implementation
- CQRS for better read/write separation
- Saga pattern for distributed transactions

### Scalability Improvements

Future enhancements include:

- Database sharding for horizontal scaling
- Enhanced event streaming capabilities
- Multi-level caching strategy
- CDN integration for static content

### Security Enhancements

I'm working on:

- mTLS between services
- Zero trust network model
- Centralized secrets management
- Comprehensive audit logging

This project represents my journey into modern microservices architecture and I'm excited to
continue evolving it with new technologies and patterns as I learn them.
