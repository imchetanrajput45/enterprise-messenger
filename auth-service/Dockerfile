# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY auth-service/pom.xml auth-service/
COPY common-lib/src common-lib/src
RUN mvn clean install -DskipTests -pl common-lib -am
COPY auth-service/src auth-service/src
RUN mvn clean package -DskipTests -pl auth-service -am

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
