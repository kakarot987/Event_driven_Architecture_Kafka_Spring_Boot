# Stage 1: build
FROM eclipse-temurin:23 AS builder
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew
# Tip: Disable the plain jar generation if you only want the bootJar
RUN ./gradlew bootJar --no-daemon

# Stage 2: run
FROM eclipse-temurin:23
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app

# Explicitly copy from the builder into the current WORKDIR (.)
# If your project name is "eureka-server", the jar is likely "eureka-server-0.0.1-SNAPSHOT.jar"
COPY --from=builder /workspace/build/libs/*.jar ./app.jar

EXPOSE 8761

# Execute it relative to the WORKDIR, or use the absolute path /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]