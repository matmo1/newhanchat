# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# 1. Copy only wrapper and pom to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# 2. Use a cache mount to avoid re-downloading jars every time
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline

# 3. Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083 
ENTRYPOINT ["java", "-jar", "app.jar"]