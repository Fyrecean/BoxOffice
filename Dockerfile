# Stage 1: Build the application
FROM gradle:7.5-jdk17 AS builder
WORKDIR /home/gradle/project
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle build --no-daemon

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /home/gradle/project/build/libs/boxoffice-1.0.0.jar app.jar
EXPOSE 8080

# Define entry point
ENTRYPOINT ["java","-jar","/app/app.jar"]
