# Multi-stage build for Matrix Agents

# Stage 1: Collect the frontend built by the azd predeploy hook
FROM scratch AS frontend-build
COPY frontend/dist /app/frontend/dist

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

# Download Application Insights Java agent for Azure Monitor telemetry
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.6.2/applicationinsights-agent-3.6.2.jar applicationinsights-agent.jar

# Copy Application Insights configuration
COPY applicationinsights.json ./applicationinsights.json

# Set ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# Enable Application Insights when Azure supplies its connection string.
ENTRYPOINT ["sh", "-c", "if [ -n \"${APPLICATIONINSIGHTS_CONNECTION_STRING:-}\" ]; then JAVA_AGENT=-javaagent:/app/applicationinsights-agent.jar; fi; exec java ${JAVA_AGENT:-} ${JAVA_OPTS:-} -XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
