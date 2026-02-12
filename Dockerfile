FROM eclipse-temurin:21 AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew

RUN ./gradlew bootJar --no-daemon

RUN ls -la build/libs/

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy all jars from build stage
COPY --from=build /app/build/libs/ ./

# Rename to app.jar (assumes only one jar exists)
RUN mv *.jar app.jar || echo "No jar file found!"

RUN addgroup --system spring && adduser --system --ingroup spring spring
RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]