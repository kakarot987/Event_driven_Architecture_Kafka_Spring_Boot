FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./

COPY common-events/build.gradle ./common-events/
COPY util/build.gradle ./util/
COPY microservices/inventory-service/build.gradle ./microservices/inventory-service/

RUN chmod +x ./gradlew

# Force Gradle wrapper download before copying all source
RUN ./gradlew --version --no-daemon

COPY . .

RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :microservices:inventory-service:bootJar \
    -x test \
    --no-daemon \
    --stacktrace

FROM eclipse-temurin:23-jre

WORKDIR /app

COPY --from=builder \
    /workspace/microservices/inventory-service/build/libs/*.jar \
    app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]