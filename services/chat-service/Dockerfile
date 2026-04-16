# 1. Build stage: Fixed 'as' casing and optimized for caching
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy only the files needed to download dependencies [cite: 1]
# This prevents the 'dependency:go-offline' step from re-running if only your code changes
COPY mvnw .
COPY .mvn/ .mvn
COPY pom.xml .

# Fix line endings and permissions for the wrapper [cite: 2]
RUN sed -i 's/\r$//' mvnw 
RUN chmod +x mvnw

# Use a BuildKit cache mount to share the Maven repo across builds
# This is the single biggest speed improvement for multiple microservices
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline

# Copy the source code AFTER the dependencies are cached 
COPY src ./src

# Build the JAR using the cache mount again
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -Dmaven.test.skip=true

# 2. Runtime stage [cite: 1]
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built artifact from the builder stage [cite: 1]
COPY --from=builder /app/target/newhanchat-*.jar app.jar

# Environment variables [cite: 1]
ENV SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/newhanchat
ENV SPRING_DATA_MONGODB_DATABASE=newhanchat
ENV TZ=Asia/Seoul

# Set timezone [cite: 1]
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]