FROM openjdk:20-jdk

EXPOSE 8081

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
