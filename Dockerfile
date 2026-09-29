# --- Build Stage ---
FROM eclipse-temurin:21-jdk-alpine AS build
# Install Maven
RUN apk add --no-cache maven
WORKDIR /app
COPY . .
ENV MAVEN_OPTS="-Xmx384m"
# Build the JAR using installed Maven
RUN mvn clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", \
  "-Xms64m", "-Xmx350m", \
  "-XX:+UseSerialGC", \
  "-XX:MaxMetaspaceSize=256m", \
  "-XX:TieredStopAtLevel=1", \
  "-XX:+UseStringDeduplication", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.jmx.enabled=false", \
  "-jar", "app.jar"]
