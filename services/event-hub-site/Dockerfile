# Stage 1: Build
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /app

# Copy poms first to maximize dependency-layer caching.
COPY event-hub-model/pom.xml /app/event-hub-model/pom.xml
COPY event-hub-site/pom.xml /app/event-hub-site/pom.xml
RUN mvn -B -ntp -f /app/event-hub-model/pom.xml dependency:go-offline

# Copy source and build local dependency first.
COPY event-hub-model/src /app/event-hub-model/src
COPY event-hub-site/src /app/event-hub-site/src
ARG COMMIT_SHA=local
ARG APP_VERSION=1.0.0
RUN mvn -B -ntp -f /app/event-hub-model/pom.xml clean install -DskipTests \
	&& mvn -B -ntp -f /app/event-hub-site/pom.xml dependency:go-offline \
	&& mvn -B -ntp -f /app/event-hub-site/pom.xml clean package spring-boot:repackage -DskipTests \
	&& cp /app/event-hub-site/target/event-hub-site-${APP_VERSION}.jar /app/event-hub-site/target/app.jar

# Stage 2: Runtime
FROM gcr.io/distroless/java17-debian12:nonroot
WORKDIR /app

LABEL maintainer="raghu.porumamilla@gmail.com"
LABEL org.opencontainers.image.source="https://github.com/raghuporumamila/event-hub"

ARG APP_VERSION=1.0.0
ARG COMMIT_SHA=local
ENV VERSION=${APP_VERSION}
ENV PORT=8080
ENV APP_COMMIT_SHA=${COMMIT_SHA}

COPY --from=build /app/event-hub-site/target/app.jar /app/app.jar

USER nonroot
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-Dserver.port=8080", "-jar", "/app/app.jar"]