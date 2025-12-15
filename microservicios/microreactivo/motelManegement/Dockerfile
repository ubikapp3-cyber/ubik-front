# Etapa build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiamos TODO el proyecto (para que exista el pom padre)
COPY . .

# Set Maven options for SSL
ENV MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true"

# Construimos SOLO el módulo motel-management
RUN mvn clean package -pl motelManegement -am -DskipTests

# Etapa runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/motelManegement/target/*.jar app.jar

EXPOSE 8083
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java","-jar","app.jar"]
