# Use an official OpenJDK 11 image with the JDK included
FROM openjdk:11-jdk-slim

# Create a directory for the application
WORKDIR /app

# Copy the JAR file from api-automation module into the container
COPY api-automation/build/libs/api-automation-1.0.0.jar .

# Define the command to run the JAR file
CMD ["java", "-jar", "api-automation-1.0.0.jar"]