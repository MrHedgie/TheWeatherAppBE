# Etap 1: budowanie aplikacji
FROM maven:3.9.6-eclipse-temurin-21 AS maven
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Etap 2: tworzenie lekkiego obrazu zbudowanej aplikacji
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=maven /app/target/*.jar app.jar

# Uruchomienie aplikacji
ENTRYPOINT ["java", "-jar", "app.jar"]
