# Multi-stage build for optimized Docker image
FROM maven:3.9.5-openjdk-21-slim AS build

WORKDIR /app

COPY pom.xml .
COPY services/user-service/pom.xml services/user-service/

RUN mvn dependency:go-offline -B -pl services/user-service

COPY services/user-service/src services/user-service/src

RUN mvn clean package -DskipTests -pl services/user-service

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache curl

RUN groupadd -r flagship && useradd -r -g flagship flagship

WORKDIR /app

COPY --from=build /app/services/user-service/target/user-service-*.jar app.jar

RUN chown flagship:flagship app.jar

USER flagship

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
