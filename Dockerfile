# Use an official Gradle image to build the application
FROM gradle:8.9.0-jdk21 AS build

# Set the working directory
WORKDIR /app

# Copy the Gradle wrapper and build files
COPY build.gradle.kts settings.gradle.kts gradle/ ./

# Copy the source code
COPY src ./src

# Build the application
RUN gradle build --no-daemon

# Use a smaller JDK image for the final stage
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/build/libs/backend-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the port the application runs on
EXPOSE 8080

# Define the entry point to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]