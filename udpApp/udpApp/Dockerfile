# Use an OpenJDK 17 base image
FROM openjdk:17

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/udp.jar /app

# Default command
CMD ["java", "-jar", "udp.jar"]

