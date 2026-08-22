# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy gradle files
COPY gradlew .
COPY gradle gradle
COPY *.gradle.kts .
COPY gradle.properties .

# Ensure gradlew is executable
RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew --no-daemon dependencies

# Copy source code
COPY src src

# Build the fat jar
RUN ./gradlew --no-daemon buildFatJar

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/build/libs/*-all.jar app.jar

# Expose Ktor port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
