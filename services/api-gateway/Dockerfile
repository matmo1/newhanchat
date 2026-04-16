# 1. Build Stage
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy Maven files first to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Use the cache mount to share dependencies with your other services
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline

# Copy source and build the JAR
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests

# 2. Runtime Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the JAR from the builder stage
# Tip: Using a wildcard *.jar makes it more resilient to version changes
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]