# Stage 1: Build application with Maven
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven descriptor and download dependencies
COPY pom.xml ./
COPY .mvn .mvn/
COPY mvnw ./
RUN chmod +x mvnw || true

# Copy source code and build package
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn clean package -DskipTests

# Stage 2: Minimal Runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create unprivileged application user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose HTTP port
EXPOSE 8080

# Environment variables for database configuration
ENV DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=smart_manufacturing_db \
    DB_USERNAME=root \
    DB_PASSWORD=rootpassword \
    SPRING_PROFILES_ACTIVE=default

# Execute Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
