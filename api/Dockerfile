# ===== STAGE 1: BUILD — có JDK + Maven, dùng xong vứt =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ===== STAGE 2: RUNTIME — chỉ JRE + jar =====
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build --chown=app:app /build/target/api-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads && chown -R app:app /app 
USER app
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
