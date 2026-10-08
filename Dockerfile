# Render uses Docker for JVM applications.
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/smart-campus-backend-1.0.0.jar app.jar
EXPOSE 8080
# Render's DB_* values are the production datasource source of truth. Ignore
# stale SPRING_DATASOURCE_* overrides that can replace the profile properties.
ENTRYPOINT ["sh", "-c", "unset SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD; exec java -jar app.jar"]
