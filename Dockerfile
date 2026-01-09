FROM eclipse-temurin:11-jdk

WORKDIR /app

COPY . .

# Donner les droits d'exécution au Maven Wrapper
RUN chmod +x mvnw

# Construire l'application
RUN ./mvnw clean package -DskipTests

# Exposer le port Render
EXPOSE 8080

# Lancer l'application
CMD ["java", "-jar", "target/transfert-api-0.0.1-SNAPSHOT.jar"]
