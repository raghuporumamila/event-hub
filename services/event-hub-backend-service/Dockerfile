# Use an official JDK runtime as a parent image
FROM eclipse-temurin:17-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the executable JAR from your host to the container
# Replace 'your-app-name.jar' with the actual name of your generated JAR
COPY target/event-hub-service-v2-1.0.0.jar app.jar

# Expose the port your Spring Boot app runs on (default is 8080)
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]