FROM gradle:jdk17 AS build
COPY --chown=gradle:gradle settings.gradle /home/gradle/service/
COPY --chown=gradle:gradle ./gpc-api-facade /home/gradle/service/gpc-api-facade
COPY --chown=gradle:gradle ./db-connector /home/gradle/service/db-connector

WORKDIR /home/gradle/service/gpc-api-facade
RUN ./gradlew --build-cache bootJar

FROM openjdk:17

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/service/gpc-api-facade/build/libs/gpc-api-facade.jar /app/gpc-api-facade.jar

ENTRYPOINT ["java", "-cp", "/app/gpc-api-facade.jar", "-Dloader.main=uk.nhs.adaptors.pss.gpc.GpcFacadeApplication", "org.springframework.boot.loader.PropertiesLauncher"]
