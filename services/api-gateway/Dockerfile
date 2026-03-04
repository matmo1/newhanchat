# Use the same Java version as your other services
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy Maven wrapper and config
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies (this layer is cached)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the JAR (skipping tests to speed up deployment)
RUN ./mvnw clean package
# Run the Gateway
# Ensure the JAR name matches your pom.xml artifactId + version
CMD ["java", "-jar", "target/api-gateway-0.0.1-SNAPSHOT.jar"]