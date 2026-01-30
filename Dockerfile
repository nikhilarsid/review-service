# Stage 1: Build the application
# Use a base image with Gradle/Java
FROM gradle:jdk17-alpine AS build
WORKDIR /app
COPY . .
# Build the JAR using Gradle
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# ✅ FIX: Gradle outputs JARs to 'build/libs', not 'target'
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the Review Service port
EXPOSE 8098

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
