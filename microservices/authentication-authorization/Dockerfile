# Multi-stage Dockerfile for building and running the Authentication-Authorization Service

FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

# Copy Gradle wrapper/config first
COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./

COPY common-events/build.gradle ./common-events/
COPY util/build.gradle ./util/
COPY microservices/authentication-authorization/build.gradle ./microservices/authentication-authorization/

RUN chmod +x ./gradlew

# Copy full project
COPY . .

# Build the bootJar
RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :microservices:authentication-authorization:bootJar \
    -x test \
    --no-daemon \
    --stacktrace

# Runtime stage
FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=builder \
    /workspace/microservices/authentication-authorization/build/libs/*.jar \
    app.jar

EXPOSE 8001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]