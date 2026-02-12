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

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
# 1. Use Ubuntu/Debian syntax for creating the user and group
RUN addgroup --system spring && adduser --system --ingroup spring spring

# 3. Copy the JAR and ensure the 'spring' user owns it
# Adding --chown ensures the non-root user can actually run the file
# 4. Switch to the non-root user
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]