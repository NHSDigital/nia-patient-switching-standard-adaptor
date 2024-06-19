FROM gradle:7.6-jdk17 AS build
COPY --chown=gradle:gradle settings.gradle /home/gradle/service/
COPY --chown=gradle:gradle ./gpc-api-facade /home/gradle/service/gpc-api-facade
COPY --chown=gradle:gradle ./common /home/gradle/service/common
COPY --chown=gradle:gradle ./db-connector /home/gradle/service/db-connector
COPY --chown=gradle:gradle ./config /home/gradle/service/config

WORKDIR /home/gradle/service/gpc-api-facade
RUN gradle --build-cache bootJar

FROM eclipse-temurin:17-jre-jammy

EXPOSE 8081

RUN mkdir /app

COPY --from=build /home/gradle/service/gpc-api-facade/build/libs/gpc-api-facade.jar /app/gpc-api-facade.jar

ENTRYPOINT ["java", "-cp", "/app/gpc-api-facade.jar", "-Dloader.main=uk.nhs.adaptors.pss.gpc.GpcFacadeApplication", "org.springframework.boot.loader.PropertiesLauncher"]
