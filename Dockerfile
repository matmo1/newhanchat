# Build stage with OpenJDK 21
FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Add this line to set execute permissions
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline
COPY src ./src
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