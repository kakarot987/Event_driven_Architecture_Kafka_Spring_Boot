FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./

COPY common-events/build.gradle ./common-events/
COPY util/build.gradle ./util/
COPY microservices/order-service/build.gradle ./microservices/order-service/

RUN chmod +x ./gradlew

COPY . .

RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :microservices:order-service:bootJar \
    -x test \
    --no-daemon \
    --stacktrace

FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=builder \
    /workspace/microservices/order-service/build/libs/*.jar \
    app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]