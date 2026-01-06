# --- Étape 1: Build avec Maven ---
# On utilise une image qui contient Maven et Java 17
FROM maven:3.8.5-openjdk-17 AS build

# On définit le répertoire de travail dans le conteneur
WORKDIR /app

# On copie d'abord le pom.xml pour mettre en cache les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline

# On copie le reste du code source
COPY src ./src

# On construit l'application et on crée le .jar (en sautant les tests, car ils sont faits dans le pipeline)
RUN mvn package -DskipTests

# --- Étape 2: Exécution ---
# On utilise une image JRE très légère pour l'exécution
FROM openjdk:17-jre-slim

WORKDIR /app

# On copie uniquement le .jar final depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# On expose le port 8081 sur lequel tourne votre application
EXPOSE 8081

# La commande pour lancer l'application au démarrage du conteneur
ENTRYPOINT ["java", "-jar", "app.jar"]
