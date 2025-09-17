# Multi-stage build for optimized Docker image
FROM maven:latest AS build

WORKDIR /app

# Copy parent POM and all service POMs to satisfy Maven multi-module requirements
COPY pom.xml .
COPY services/api-gateway/pom.xml services/api-gateway/
COPY services/user-service/pom.xml services/user-service/
COPY services/order-service/pom.xml services/order-service/
COPY services/payment-service/pom.xml services/payment-service/
COPY services/inventory-service/pom.xml services/inventory-service/
COPY services/streaming-service/pom.xml services/streaming-service/

# Create placeholder directories for all services to satisfy Maven
RUN mkdir -p services/user-service/src/main/java services/user-service/src/main/resources services/user-service/src/test/java services/user-service/src/test/resources
RUN mkdir -p services/order-service/src/main/java services/order-service/src/main/resources services/order-service/src/test/java services/order-service/src/test/resources
RUN mkdir -p services/payment-service/src/main/java services/payment-service/src/main/resources services/payment-service/src/test/java services/payment-service/src/test/resources
RUN mkdir -p services/inventory-service/src/main/java services/inventory-service/src/main/resources services/inventory-service/src/test/java services/inventory-service/src/test/resources
RUN mkdir -p services/streaming-service/src/main/java services/streaming-service/src/main/resources services/streaming-service/src/test/java services/streaming-service/src/test/resources

# Copy the actual source code for the API Gateway
COPY services/api-gateway/src services/api-gateway/src

# Build only the API Gateway service
RUN mvn clean package spring-boot:repackage -DskipTests -pl services/api-gateway

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
RUN addgroup -g 1000 flagship && adduser -D -s /bin/sh -u 1000 -G flagship flagship

WORKDIR /app

COPY --from=build /app/services/api-gateway/target/api-gateway-*.jar app.jar
RUN chown flagship:flagship app.jar

USER flagship

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
