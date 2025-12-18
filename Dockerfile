# Multi-stage Dockerfile for Spring Boot Microservices

# Stage 1: Build stage
FROM maven:3.9.9-amazoncorretto-21 AS builder

WORKDIR /app

# Copy pom files
COPY pom.xml ./
COPY boss-common/pom.xml ./boss-common/
COPY boss-gateway/pom.xml ./boss-gateway/
COPY boss-user-service/pom.xml ./boss-user-service/
COPY boss-job-service/pom.xml ./boss-job-service/
COPY boss-chat-service/pom.xml ./boss-chat-service/
COPY boss-ai-service/pom.xml ./boss-ai-service/
COPY boss-search-service/pom.xml ./boss-search-service/

# Copy source code
COPY boss-common/src ./boss-common/src
COPY boss-gateway/src ./boss-gateway/src
COPY boss-user-service/src ./boss-user-service/src
COPY boss-job-service/src ./boss-job-service/src
COPY boss-chat-service/src ./boss-chat-service/src
COPY boss-ai-service/src ./boss-ai-service/src
COPY boss-search-service/src ./boss-search-service/src

# Build the project
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM amazoncorretto:21-alpine

# Create app directory
WORKDIR /app

# Copy jar files from builder stage
COPY --from=builder /app/boss-gateway/target/*.jar ./boss-gateway.jar
COPY --from=builder /app/boss-user-service/target/*.jar ./boss-user-service.jar
COPY --from=builder /app/boss-job-service/target/*.jar ./boss-job-service.jar
COPY --from=builder /app/boss-chat-service/target/*.jar ./boss-chat-service.jar
COPY --from=builder /app/boss-ai-service/target/*.jar ./boss-ai-service.jar
COPY --from=builder /app/boss-search-service/target/*.jar ./boss-search-service.jar

# Expose ports (can be overridden)
EXPOSE 8080 8081 8082 8083 8084 8085

# Default command - run the gateway service by default
CMD ["java", "-jar", "boss-gateway.jar"]