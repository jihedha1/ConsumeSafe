# --- Étape 1: Build ---
# Utilise une image Maven pour construire le .jar
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# --- Étape 2: Run ---
# On utilise une image JRE légère et sécurisée d'Eclipse Temurin sur base Alpine
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
# Copie uniquement le .jar de l'étape de build
COPY --from=build /app/target/*.jar app.jar
# Expose le port sur lequel tourne Spring Boot
EXPOSE 8081
# Commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
