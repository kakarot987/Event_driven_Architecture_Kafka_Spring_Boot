FROM eclipse-temurin:23-jdk AS builder

WORKDIR /workspace

COPY gradle/ ./gradle/
COPY settings.gradle gradlew gradlew.bat ./
COPY spring_cloud/config-server/build.gradle ./spring_cloud/config-server/

RUN chmod +x ./gradlew

COPY . .

RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle,sharing=locked \
    ./gradlew :spring_cloud:config-server:bootJar \
    -x test \
    --no-daemon \
    --stacktrace

FROM eclipse-temurin:23-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder \
    /workspace/spring_cloud/config-server/build/libs/*.jar \
    app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]