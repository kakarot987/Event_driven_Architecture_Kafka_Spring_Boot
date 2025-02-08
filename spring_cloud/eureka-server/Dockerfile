FROM openjdk:23 as builder
ARG JAR_FILE=./build/libs/config-server-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
#WORKDIR /workspace
#ADD ./build/libs/config-server-0.0.1-SNAPSHOT.jar app.jar
#RUN java -Djarmode=layertools -jar app.jar extract || (echo "Layertools extraction failed"; ls -la)

#FROM openjdk:23
#WORKDIR /app
# Temporarily avoid copying for debugging
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "/app.jar"]
