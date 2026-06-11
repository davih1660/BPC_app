FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Camada separada para cache de dependências (e retry em rede instável)
COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests \
    || mvn -B dependency:resolve dependency:resolve-plugins -DskipTests

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
