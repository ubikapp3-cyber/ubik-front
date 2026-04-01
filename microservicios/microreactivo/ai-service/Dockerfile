# ---------- BUILD ----------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY aiService/pom.xml .
RUN mvn dependency:go-offline

COPY aiService/src ./src

RUN mvn clean package -DskipTests


# ---------- RUNTIME ----------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY --from=builder /app/target/ai-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]