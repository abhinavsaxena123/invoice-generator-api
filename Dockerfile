# ================= STAGE 1: Build the application =================
# Use a base image with Java 21 and Maven to build the project
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project file first to leverage Docker layer caching
COPY pom.xml .

# Copy the Maven wrapper (if you have one)
COPY .mvn/ .mvn
COPY mvnw .

# Download dependencies
RUN mvn dependency:go-offline

# Copy the rest of your source code
COPY src ./src

# Package the application into an executable JAR
RUN mvn package -DskipTests

# ================= STAGE 2: Create the final, small image =================
# Use a minimal base image with just the Java 21 Runtime Environment
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the executable JAR file from the 'builder' stage
COPY --from=builder /app/target/invoiceGeneratorApi-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application runs on
EXPOSE 8080

# The command to run your application
ENTRYPOINT ["java", "-jar", "app.jar"]