# Dockerfile
FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/test-backend.jar .
EXPOSE 4000
ENTRYPOINT ["java", "-jar", "test-backend.jar"]
