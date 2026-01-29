# Stage 1: Build the application
# Using Maven image to build the project
FROM gradle:jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Run the application
# ✅ FIX: Using 'eclipse-temurin' as requested for lighter, faster runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copy the JAR from the build stage (Maven creates target folder)
COPY --from=build /app/target/*.jar app.jar

# Expose the Review Service port
EXPOSE 8098

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]