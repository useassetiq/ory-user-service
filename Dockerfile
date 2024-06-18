FROM azul/zulu-openjdk-alpine:21-jre-latest
# Set the environment variable for the jar file

COPY build/libs/auth-service.jar /app/auth-service.jar

# Set the working directory to /app
WORKDIR /app

# Run the jar file when the container launches
ENTRYPOINT ["java", "-jar", "auth-service.jar"]