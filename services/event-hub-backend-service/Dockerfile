# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image (Mimicking Jib's layering)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Jib stores dependencies, resources, and classes separately for speed.
# We extract the Spring Boot fat JAR to replicate this.
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../../app.jar)

# Copy layers manually to ensure high cache hit rate
COPY --from=build /app/target/dependency/BOOT-INF/lib /app/lib
COPY --from=build /app/target/dependency/META-INF /app/META-INF
COPY --from=build /app/target/dependency/BOOT-INF/classes /app/classes

# Run the app using the explicit classpath (Standard Jib behavior)
ENTRYPOINT ["java","-cp","app/resources:app/classes:app/lib/*","com.yourpackage.YourMainApplication"]