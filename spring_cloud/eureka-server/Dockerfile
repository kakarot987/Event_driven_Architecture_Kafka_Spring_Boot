# Multi-stage Dockerfile for Eureka Server

# Stage 1: Build
FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

# Copy Gradle wrapper and build configuration
COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./

# Copy Eureka module build file
COPY spring_cloud/eureka-server/build.gradle ./spring_cloud/eureka-server/

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Copy complete project source
COPY . .

# Build Eureka Server
RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :spring_cloud:eureka-server:bootJar \
    -x test \
    --no-daemon \
    --stacktrace


# Stage 2: Runtime
FROM eclipse-temurin:23-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy generated Spring Boot JAR
COPY --from=builder \
    /workspace/spring_cloud/eureka-server/build/libs/*.jar \
    app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/app.jar"]