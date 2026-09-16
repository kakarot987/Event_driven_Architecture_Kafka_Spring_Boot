/* Multi-stage Dockerfile for building and running the Shipping Service */

# Builder stage: uses official Gradle image with JDK 17 to run the Gradle build
FROM gradle:8.6-jdk17 AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . ./
# Build the bootJar (skip tests to make image build faster during development)
RUN gradle clean bootJar -x test --no-daemon

# Runtime stage: small JRE image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the boot jar from the builder stage
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

