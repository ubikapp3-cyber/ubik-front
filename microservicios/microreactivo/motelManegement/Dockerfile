# Etapa build
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiamos TODO el proyecto (para que exista el pom padre)
COPY . .

# Actualizar certificados (frecuente problema en Alpine)
RUN apk update && apk add --no-cache ca-certificates

# Construimos SOLO el módulo motel-management
RUN mvn clean package -pl motelManegement -am -DskipTests -ntp

# Etapa runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

# tzdata necesario en Alpine para que TZ=America/Bogota funcione
RUN apk add --no-cache wget tzdata && \
    cp /usr/share/zoneinfo/America/Bogota /etc/localtime && \
    echo "America/Bogota" > /etc/timezone

USER spring:spring

COPY --from=build /app/motelManegement/target/*.jar app.jar

EXPOSE 8083
ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=America/Bogota
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
