FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
# Actualizar certificados (frecuente problema en Alpine)
RUN apk update && apk add --no-cache ca-certificates

RUN mvn clean package -pl paymentService -am -DskipTests -ntp

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=build /app/paymentService/target/*.jar app.jar
EXPOSE 8085
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]