# ---------- Build stage ----------
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Copy Gradle files
COPY build.gradle settings.gradle ./
COPY services/api-gateway/build.gradle ./services/api-gateway/

# Download dependencies (this layer will be cached if build.gradle doesn't change)
RUN gradle dependencies --no-daemon

# Copy source code
COPY services/api-gateway/src ./services/api-gateway/src

# Build the application
RUN gradle :services:api-gateway:bootJar --no-daemon

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the runnable jar built above
COPY --from=build /app/services/api-gateway/build/libs/*.jar app.jar

# Railway provides PORT; bind to it
ENV PORT=8081
EXPOSE 8081

# Optimized JVM settings for Railway memory constraints
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom -Dreactor.netty.ioWorkerCount=2"

# Start the app on the assigned port
CMD ["sh","-c","java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT} -jar /app/app.jar"]