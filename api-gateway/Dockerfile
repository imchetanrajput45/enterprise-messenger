# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY api-gateway/pom.xml api-gateway/
COPY common-lib/src common-lib/src
RUN mvn clean install -DskipTests -pl common-lib -am
COPY api-gateway/src api-gateway/src
RUN mvn clean package -DskipTests -pl api-gateway -am

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
