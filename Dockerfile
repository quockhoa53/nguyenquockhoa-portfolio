# Stage 1: Build application with Gradle
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy gradle wrapper and configuration files first to leverage Docker cache
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source code and build jar
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Lightweight JRE Container for Production
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy executable jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose standard Spring Boot port
EXPOSE 8080

# Configure memory limits optimized for free tier instances (512MB RAM) and fast entropy
ENV JAVA_OPTS="-Xms128m -Xmx320m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -Djava.security.egd=file:/dev/./urandom"

# Start Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
