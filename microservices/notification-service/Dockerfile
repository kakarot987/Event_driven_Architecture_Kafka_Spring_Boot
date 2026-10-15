# Multi-stage Dockerfile for building and running the Notification Service

# Builder stage
FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build configuration
COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./

# Copy module build files
COPY common-events/build.gradle ./common-events/
COPY util/build.gradle ./util/
COPY microservices/notification-service/build.gradle ./microservices/notification-service/

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Copy complete project source
COPY . .

# Build Notification Service
RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :microservices:notification-service:bootJar \
    -x test \
    --no-daemon \
    --stacktrace


# Runtime stage
FROM eclipse-temurin:23-jre

WORKDIR /app

# Copy generated Spring Boot JAR
COPY --from=builder \
    /workspace/microservices/notification-service/build/libs/*.jar \
    app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]