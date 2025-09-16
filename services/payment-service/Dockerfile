# Multi-stage build for optimized Docker image
FROM maven:3.9.5-openjdk-21-slim AS build

WORKDIR /app

COPY pom.xml .
COPY services/payment-service/pom.xml services/payment-service/

RUN mvn dependency:go-offline -B -pl services/payment-service

COPY services/payment-service/src services/payment-service/src

RUN mvn clean package -DskipTests -pl services/payment-service

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

RUN groupadd -r flagship && useradd -r -g flagship flagship

WORKDIR /app

COPY --from=build /app/services/payment-service/target/payment-service-*.jar app.jar

RUN chown flagship:flagship app.jar

USER flagship

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
