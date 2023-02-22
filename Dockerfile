FROM gradle:jdk17-alpine as compile
COPY . /home/source/java
WORKDIR /home/source/java
# Default gradle user is `gradle`. We need to add permission on working directory for gradle to build.
USER root
RUN chown -R gradle /home/source/java
USER gradle
RUN gradle clean build

FROM gradle:jdk17-alpine
WORKDIR /home/application/java
COPY /root/application_default_credentials.json /home/application/java/application_default_credentials.json
COPY --from=compile "/home/source/java/build/libs/user-service-0.0.1-SNAPSHOT.jar" .
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/home/application/java/user-service-0.0.1-SNAPSHOT.jar"]
