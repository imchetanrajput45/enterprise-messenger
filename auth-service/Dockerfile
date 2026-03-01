# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy root pom and ALL module poms (needed for Maven multi-module reactor)
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY service-registry/pom.xml service-registry/
COPY config-server/pom.xml config-server/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY user-service/pom.xml user-service/
COPY messaging-service/pom.xml messaging-service/

# Build common-lib first (dependency of all services)
COPY common-lib/src common-lib/src
RUN mvn clean install -DskipTests -pl common-lib -am

# Build auth-service
COPY auth-service/src auth-service/src
RUN mvn clean package -DskipTests -pl auth-service -am

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/target/auth-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
