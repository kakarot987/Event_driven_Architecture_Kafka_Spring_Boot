# Multi-stage Dockerfile for building and running the Shipping Service

FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./

COPY common-events/build.gradle ./common-events/
COPY util/build.gradle ./util/
COPY microservices/shipping-service/build.gradle ./microservices/shipping-service/

RUN chmod +x ./gradlew

COPY . .

RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :microservices:shipping-service:bootJar \
    -x test \
    --no-daemon \
    --stacktrace

FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=builder \
    /workspace/microservices/shipping-service/build/libs/*.jar \
    app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]