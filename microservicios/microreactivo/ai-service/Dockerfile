# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar TODO el proyecto (incluye pom padre)
COPY . .

# (Opcional pero recomendado en Alpine)
RUN apk update && apk add --no-cache ca-certificates

# Compilar SOLO el módulo ai-service
RUN mvn clean package \
    -pl ai-service \
    -am \
    -DskipTests -ntp

# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR del ai-service
COPY --from=build /app/ai-service/target/*.jar app.jar

EXPOSE 8086

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8086/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]