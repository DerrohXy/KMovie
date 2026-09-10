# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Run stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# ffmpeg provides the ffprobe binary VideoDetailsService shells out to.
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --create-home --shell /bin/bash kmovie
COPY --from=build /app/target/kmovie.jar app.jar
RUN chown -R kmovie:kmovie /app
USER kmovie
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
