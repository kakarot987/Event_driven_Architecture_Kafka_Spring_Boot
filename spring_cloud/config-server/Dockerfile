FROM openjdk:23 as builder
WORKDIR extracted
ADD build/libs/config-server-0.0.1-SNAPSHOT.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:23

WORKDIR application
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

EXPOSE 8888

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
