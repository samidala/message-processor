# Stage 1: Build the JAR
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/message-processor-0.0.1-SNAPSHOT.jar app.jar

# Default environment variables (can be overridden by K8s)
ENV JAVA_OPTS=""
ENV SERVER_PORT=8080

# Expose Actuator / Prometheus port
EXPOSE 9090

# Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
