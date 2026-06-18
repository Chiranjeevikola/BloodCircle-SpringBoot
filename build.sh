#!/usr/bin/env bash
set -o errexit

# ===== Build React frontend =====
cd frontend
npm install
npm run build
cd ..

# ===== Copy React build into Spring Boot static resources =====
mkdir -p backend/src/main/resources/static
cp -r frontend/build/* backend/src/main/resources/static/

# ===== Build Spring Boot backend (with React embedded) =====
cd backend
chmod +x mvnw
./mvnw clean package -DskipTests
cd ..
