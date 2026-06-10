# ============================================================
# DOCKERFILE - TheNews Application
# ============================================================
# Multi-stage build for optimal image size and security
# ============================================================

# ------------------------------------------------------------
# Stage 1: Build
# ------------------------------------------------------------
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN gradle clean build -x test --no-daemon

# ------------------------------------------------------------
# Stage 2: Runtime
# ------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="MMVA Team"
LABEL description="TheNews Application API"

WORKDIR /app

# Install wget for health checks
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create directories for data and logs
RUN mkdir -p /app/data/media/news \
    /app/data/media/avatars \
    /var/log/news-app \
    && chown -R appuser:appgroup /app /var/log/news-app

# Copy JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
