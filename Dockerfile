# Image Java 11
FROM eclipse-temurin:11-jdk

# Dossier de travail
WORKDIR /app

# Copier le projet
COPY . .

# Construire l'application
RUN ./mvnw clean package -DskipTests

# Exposer le port Render
EXPOSE 8080

# Démarrer l'application
CMD ["java", "-jar", "target/transfert-api-0.0.1-SNAPSHOT.jar"]
