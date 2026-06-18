# ===== Stage 1: Build React frontend =====
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ===== Stage 2: Build Spring Boot backend =====
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY backend/src ./src

# Copy React build into Spring Boot static resources
COPY --from=frontend-build /app/frontend/build ./src/main/resources/static/

RUN mvn clean package -DskipTests -B

# ===== Stage 3: Production image =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=backend-build /app/backend/target/bloodcircle-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
