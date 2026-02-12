FROM eclipse-temurin:21

# 1. Use Ubuntu/Debian syntax for creating the user and group
RUN addgroup --system spring && adduser --system --ingroup spring spring

ARG JAR_FILE=build/libs/*.jar

# 3. Copy the JAR and ensure the 'spring' user owns it
# Adding --chown ensures the non-root user can actually run the file
COPY ${JAR_FILE} app.jar
# 4. Switch to the non-root user
USER spring:spring

ENTRYPOINT ["java","-jar","/app.jar"]