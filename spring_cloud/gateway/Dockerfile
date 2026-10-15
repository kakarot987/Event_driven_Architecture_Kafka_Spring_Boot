# Multi-stage Dockerfile for building and running the Gateway

FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./

COPY spring_cloud/gateway/build.gradle ./spring_cloud/gateway/

RUN chmod +x ./gradlew

COPY . .

RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :spring_cloud:gateway:bootJar \
    -x test \
    --no-daemon \
    --stacktrace

FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=builder \
    /workspace/spring_cloud/gateway/build/libs/*.jar \
    app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]