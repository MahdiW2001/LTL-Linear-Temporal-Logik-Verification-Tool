# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
# Pre-fetch deps for faster incremental builds
RUN mvn -q -B dependency:go-offline
COPY src ./src
# Build (skip tests for speed; remove -DskipTests to run them)
RUN mvn -q -B package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy the built jar (only one artifact expected)
COPY --from=build /workspace/target/*.jar app.jar

# Environment (adjust memory if needed)
ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    SPRING_PROFILES_ACTIVE=default

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]