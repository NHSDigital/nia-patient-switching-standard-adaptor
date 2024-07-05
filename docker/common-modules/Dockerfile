FROM gradle:8.8-jdk17 AS build
COPY --chown=gradle:gradle settings.gradle /home/gradle/service/
COPY --chown=gradle:gradle ./common /home/gradle/service/common
COPY --chown=gradle:gradle ./db-connector /home/gradle/service/db-connector
COPY --chown=gradle:gradle ./schema /home/gradle/service/schema
COPY --chown=gradle:gradle ./config /home/gradle/service/config

WORKDIR /home/gradle/service

ENTRYPOINT ["gradle", "check"]
