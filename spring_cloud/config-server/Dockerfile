FROM openjdk:20-jdk

EXPOSE 9000

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
