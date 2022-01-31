FROM gradle:jdk17

COPY --chown=gradle:gradle settings.gradle /home/gradle/service/
COPY --chown=gradle:gradle ./db-connector /home/gradle/service/db-connector
WORKDIR /home/gradle/service/db-connector

ENTRYPOINT ["./gradlew", "update"]
