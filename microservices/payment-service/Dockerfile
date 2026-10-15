# Multi-stage Dockerfile for building and running the Payment Service

# Builder stage
FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build configuration files
COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./

# Copy module build files
COPY common-events/build.gradle ./common-events/
COPY util/build.gradle ./util/
COPY microservices/payment-service/build.gradle ./microservices/payment-service/

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Copy complete project source
COPY . .

# Build the bootJar for payment-service
RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :microservices:payment-service:bootJar \
    -x test \
    --no-daemon \
    --stacktrace


# Runtime stage
FROM eclipse-temurin:23-jre

WORKDIR /app

# Copy the generated Spring Boot JAR
COPY --from=builder \
    /workspace/microservices/payment-service/build/libs/*.jar \
    app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]