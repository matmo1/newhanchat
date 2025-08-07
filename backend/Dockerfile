# Build stage with OpenJDK 21
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /app

# First copy just the mvnw file to verify it's there
COPY mvnw .
RUN sed -i 's/\r$//' mvnw  # Remove Windows CRLF line endings if present
RUN chmod +x mvnw

# Then copy the rest
COPY .mvn/ .mvn
COPY pom.xml .
COPY src ./src

RUN ./mvnw dependency:go-offline
RUN ./mvnw clean package -DskipTests

# Runtime stage with OpenJDK 21 JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/newhanchat-*.jar app.jar

# Environment variables for MongoDB
ENV SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/newhanchat
ENV SPRING_DATA_MONGODB_DATABASE=newhanchat

# Set timezone (optional)
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]