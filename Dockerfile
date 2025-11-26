# 1. Build Stage
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle ./
COPY gradlew ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# 2. Run Stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]