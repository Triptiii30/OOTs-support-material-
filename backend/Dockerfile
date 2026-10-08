# ===================================================================
# Stage 1: Build application with Maven
# ===================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# ===================================================================
# Stage 2: Minimal, secure runtime image
# ===================================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create unprivileged application user for security
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose HTTP port
EXPOSE 8081

# Default environment variables
ENV PORT=8081 \
    DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=smart_manufacturing_db \
    DB_USERNAME=root \
    DB_PASSWORD=rootpassword \
    SPRING_PROFILES_ACTIVE=default

# Execute Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
