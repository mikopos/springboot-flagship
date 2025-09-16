# Multi-stage build for optimized Docker image
FROM maven:3.9.5-openjdk-21-slim AS build

WORKDIR /app

COPY pom.xml .
COPY services/api-gateway/pom.xml services/api-gateway/
RUN mvn dependency:go-offline -B -pl services/api-gateway

COPY services/api-gateway/src services/api-gateway/src
RUN mvn clean package -DskipTests -pl services/api-gateway

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
RUN groupadd -r flagship && useradd -r -g flagship flagship

WORKDIR /app

COPY --from=build /app/services/api-gateway/target/api-gateway-*.jar app.jar
RUN chown flagship:flagship app.jar

USER flagship

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
