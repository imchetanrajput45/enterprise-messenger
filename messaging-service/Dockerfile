# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY messaging-service/pom.xml messaging-service/
COPY common-lib/src common-lib/src
RUN mvn clean install -DskipTests -pl common-lib -am
COPY messaging-service/src messaging-service/src
RUN mvn clean package -DskipTests -pl messaging-service -am

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/messaging-service/target/messaging-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
