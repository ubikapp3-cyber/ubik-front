# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar TODO el proyecto (incluye pom padre)
COPY . .

# Set Maven options for SSL
ENV MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true"

# Compilar SOLO el módulo notificationService
RUN mvn clean package \
    -pl notificationService \
    -am \
    -DskipTests

# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR del servicio de notificaciones
COPY --from=build /app/notificationService/target/*.jar app.jar

EXPOSE 8084

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]