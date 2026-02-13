# Stage 1: Build
FROM eclipse-temurin:21 AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /src

COPY --from=build /src/build/libs/*.jar app.jar

RUN addgroup --system spring && adduser --system --ingroup spring spring
RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

# This should point to app.jar in /app directory
ENTRYPOINT ["java", "-jar", "app.jar"]