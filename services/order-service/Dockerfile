# Multi-stage build for optimized Docker image
FROM maven:3.9.5-openjdk-21-slim AS build

WORKDIR /app

COPY pom.xml .
COPY services/order-service/pom.xml services/order-service/

RUN mvn dependency:go-offline -B -pl services/order-service

COPY services/order-service/src services/order-service/src

RUN mvn clean package -DskipTests -pl services/order-service

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

RUN groupadd -r flagship && useradd -r -g flagship flagship

WORKDIR /app

COPY --from=build /app/services/order-service/target/order-service-*.jar app.jar

RUN chown flagship:flagship app.jar

USER flagship

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
