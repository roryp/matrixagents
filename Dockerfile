# Multi-stage build for Matrix Agents

# Stage 1: Build frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy frontend build to Quarkus static resources location
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/META-INF/resources/

# Copy source code and build with uber-jar for Quarkus
COPY src ./src
RUN mvn package -DskipTests -B -Dquarkus.package.jar.type=uber-jar

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -D appuser

# Copy the built JAR (Quarkus uber-jar has -runner suffix)
COPY --from=backend-build /app/target/*-runner.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check - Quarkus uses /q/health
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/q/health || exit 1

# Run the application with optimized JVM settings
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
