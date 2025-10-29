# ---------- Build stage ----------
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY services/payment-service/build.gradle ./services/payment-service/

# Download dependencies (this layer will be cached if build.gradle doesn't change)
RUN gradle dependencies --no-daemon

# Copy source code
COPY services/payment-service/src ./services/payment-service/src

# Build the application
RUN gradle :services:payment-service:bootJar --no-daemon

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the runnable jar built above
COPY --from=build /app/services/payment-service/build/libs/*.jar app.jar

# Railway provides PORT; bind to it
ENV PORT=8084
EXPOSE 8084

# Optimized JVM settings for Railway memory constraints
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom"

# Start the app on the assigned port
CMD ["sh","-c","java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT} -jar /app/app.jar"]