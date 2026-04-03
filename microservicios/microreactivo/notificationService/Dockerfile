# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar TODO el proyecto (incluye pom padre)
COPY . .

# Actualizar certificados (frecuente problema en Alpine)
RUN apk update && apk add --no-cache ca-certificates

# Compilar SOLO el módulo notificationService (con logs detallados y sin progreso de transferencia)
RUN mvn clean package \
    -pl notificationService \
    -am \
    -DskipTests -ntp

# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring

# tzdata necesario en Alpine para que TZ=America/Bogota funcione
RUN apk add --no-cache wget tzdata && \
    cp /usr/share/zoneinfo/America/Bogota /etc/localtime && \
    echo "America/Bogota" > /etc/timezone

USER spring:spring

# Copiar el JAR del servicio de notificaciones
COPY --from=build /app/notificationService/target/*.jar app.jar

EXPOSE 8084

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=America/Bogota

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]