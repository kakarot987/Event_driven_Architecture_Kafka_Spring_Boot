FROM eclipse-temurin:23-jdk
WORKDIR /app
COPY . .
RUN ./gradlew clean build
EXPOSE 8080
CMD ["java", "-jar", "build/libs/config-repo.jar"]
