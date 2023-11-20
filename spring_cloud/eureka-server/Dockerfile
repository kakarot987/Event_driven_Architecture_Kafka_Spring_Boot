FROM openjdk:20-jdk

EXPOSE 9001

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
