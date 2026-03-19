# Stage 1: Build
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app

# Copy poms first to maximize dependency-layer caching.
COPY event-hub-model/pom.xml /app/event-hub-model/pom.xml
COPY event-hub-schema/pom.xml /app/event-hub-schema/pom.xml
RUN mvn -B -ntp -f /app/event-hub-model/pom.xml dependency:go-offline


# Copy source and build local dependency first
COPY event-hub-model/src /app/event-hub-model/src
COPY event-hub-schema/src /app/event-hub-schema/src
ARG COMMIT_SHA=local
ARG APP_VERSION=1.0.0
RUN mvn -B -ntp -f /app/event-hub-model/pom.xml clean install -DskipTests \
	&& mvn -B -ntp -f /app/event-hub-schema/pom.xml dependency:go-offline \
	&& mvn -B -ntp -f /app/event-hub-schema/pom.xml clean package -DskipTests \
	&& cp /app/event-hub-schema/target/event-hub-schema-${APP_VERSION}.jar /app/event-hub-schema/target/app.jar

# Stage 2: Runtime
# Using Google's Distroless for a minimal, non-root, secure base
FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app

# Metadata labels
LABEL maintainer="raghu.porumamilla@gmail.com"
LABEL org.opencontainers.image.source="https://github.com/raghuporumamila/event-hub"

ARG APP_VERSION=1.0.0
ARG COMMIT_SHA=local
ENV VERSION=${APP_VERSION}
ENV PORT=8080
ENV APP_COMMIT_SHA=${COMMIT_SHA}

# Copy the built artifact from the build stage
COPY --from=build /app/event-hub-schema/target/app.jar /app/app.jar

USER nonroot
EXPOSE 8080

# Distroless has no shell, so use a fixed port arg and expose PORT for platforms.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Dserver.port=8080", "-jar", "/app/app.jar"]
