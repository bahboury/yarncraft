# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built jar file into the container
# Note: You must run 'mvn clean package' locally before building this image
COPY target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]