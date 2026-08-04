# ============================================================================
# Spring Boot multi-stage Dockerfile (Maven/JDK → JRE Alpine)
# TICKET-ADV146
# ============================================================================

# Stage 1: Build with Temurin JDK 25 + Maven
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw -q -B dependency:go-offline || true
COPY src src
RUN ./mvnw -q -B -DskipTests package && \
    mv target/reconx-*.jar app.jar

# Stage 2: Runtime with Temurin JRE 25 Alpine
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S reconx && adduser -S reconx -G reconx
USER reconx
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]