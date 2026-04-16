# Stage 1: Build stage
# Using 'AS' in uppercase to match 'FROM' and avoid the Casing warning
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy only the files needed to install dependencies
# This layer is cached unless pom.xml or the wrapper changes
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw

# Use a cache mount for the Maven local repository. 
# This prevents re-downloading the internet every time you build.
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline

# Copy source code and build the application
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -Dmaven.test.skip=true

# Stage 2: Runtime stage
# We use the JRE (Runtime) instead of the JDK to keep the image small
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy only the built JAR from the first stage
COPY --from=build /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]