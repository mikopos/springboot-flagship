# ---------- Build stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only files needed for a fast, cacheable build
COPY pom.xml ./
COPY services ./services

# Build just this module (+ parent & deps)
# -q for quieter logs, -DskipTests for faster CI
RUN mvn -q -DskipTests -Prailway -pl services/inventory-service -am package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the runnable jar built above
COPY --from=build /app/services/inventory-service/target/*jar app.jar

# Railway provides PORT; bind to it
ENV PORT=8085
EXPOSE 8085

# Optimized JVM settings for Railway memory constraints
ENV JAVA_TOOL_OPTIONS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -Djava.security.egd=file:/dev/./urandom"

# Start the app on the assigned port
CMD ["sh","-c","java $JAVA_TOOL_OPTIONS -Dserver.port=${PORT} -jar /app/app.jar"]