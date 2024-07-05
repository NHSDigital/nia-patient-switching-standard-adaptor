FROM gradle:8.8-jdk17

COPY --chown=gradle:gradle settings.gradle /home/gradle/service/
COPY --chown=gradle:gradle ./common /home/gradle/service/common
COPY --chown=gradle:gradle ./config /home/gradle/service/config
COPY --chown=gradle:gradle ./db-connector /home/gradle/service/db-connector
WORKDIR /home/gradle/service/db-connector

ENTRYPOINT ["gradle", "update"]
