# First stage: Build the application
FROM eclipse-temurin:23-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and build files first for caching
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Make the Gradle wrapper executable
RUN chmod +x ./gradlew

# Download Gradle dependencies (this step will be cached)
RUN ./gradlew clean build --no-daemon

# Copy the rest of the application
COPY . .

# Second stage: Create a smaller runtime image
FROM eclipse-temurin:23-jdk

WORKDIR /app

# Copy the build artifacts from the previous stage
COPY --from=build /app/build/libs/config-repo.jar ./config-repo.jar

# Expose the port the app will run on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "config-repo.jar"]
