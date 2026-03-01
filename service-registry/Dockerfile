# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY service-registry/pom.xml service-registry/
# Build common-lib first as it's a dependency
COPY common-lib/src common-lib/src
RUN mvn clean install -DskipTests -pl common-lib -am

# Build service-registry
COPY service-registry/src service-registry/src
RUN mvn clean package -DskipTests -pl service-registry -am

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/service-registry/target/service-registry-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
