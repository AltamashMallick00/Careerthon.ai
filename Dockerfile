# --- Build Stage ---
FROM eclipse-temurin:21-jdk-alpine AS build
# Install Maven
RUN apk add --no-cache maven
WORKDIR /app
COPY . .
# Build the JAR using installed Maven
RUN mvn clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Docker health check — retries for up to 3 minutes (Render uses its own check too)
HEALTHCHECK --interval=30s --timeout=15s --start-period=90s --retries=6 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-Xms32m", "-Xmx200m", \
  "-XX:+UseSerialGC", \
  "-XX:MaxMetaspaceSize=96m", \
  "-XX:TieredStopAtLevel=1", \
  "-XX:+UseStringDeduplication", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.jmx.enabled=false", \
  "-jar", "app.jar"]

