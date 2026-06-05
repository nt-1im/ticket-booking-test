# --- Build Stage ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cached layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# --- Run Stage ---
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/target/ticket-booking-0.0.1-SNAPSHOT.jar app.jar

# Render exposes PORT environment variable which we bind to server.port
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
