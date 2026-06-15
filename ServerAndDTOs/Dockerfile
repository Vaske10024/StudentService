FROM maven:3.8.8-eclipse-temurin-11 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY dtos/pom.xml dtos/pom.xml
COPY server/pom.xml server/pom.xml

COPY dtos/src dtos/src
COPY server/src server/src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /workspace/server/target/server-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
