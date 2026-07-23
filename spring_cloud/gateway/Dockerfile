FROM eclipse-temurin:23 AS builder

EXPOSE 9002

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
