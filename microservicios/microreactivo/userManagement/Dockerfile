# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiamos TODO el proyecto (incluye el pom padre)
COPY . .

# Actualizar certificados (frecuente problema en Alpine)
RUN apk update && apk add --no-cache ca-certificates

# Compilamos SOLO el módulo userManagement
RUN mvn clean package \
    -pl userManagement \
    -am \
    -DskipTests -ntp -e

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

# Copiar el JAR correcto (la carpeta física es userManagement)
COPY --from=build /app/userManagement/target/*.jar app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=America/Bogota

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
