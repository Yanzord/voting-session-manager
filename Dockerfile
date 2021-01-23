FROM openjdk:8
WORKDIR /
COPY /build/libs/*.war voting-session-service.war
ENTRYPOINT ["java", "-jar", "voting-session-service.war"]