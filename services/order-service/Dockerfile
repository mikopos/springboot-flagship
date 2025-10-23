# Multi-stage build for optimized image size
FROM openjdk:21-jdk-slim as builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY ../../pom.xml .
COPY ../../.mvn ../../.mvn
COPY ../../mvnw .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY . .

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/target/order-service-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]