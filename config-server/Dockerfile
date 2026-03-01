# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY config-server/pom.xml config-server/
COPY common-lib/src common-lib/src
RUN mvn clean install -DskipTests -pl common-lib -am
COPY config-server/src config-server/src
RUN mvn clean package -DskipTests -pl config-server -am

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/config-server/target/config-server-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
