# BloodCircle

A full-stack blood donation management platform built with **React** frontend and **Spring Boot** REST API backend.

## Features
- **Donor Management** – Register, manage profile, toggle availability
- **Patient Management** – Register, manage requests, search compatible donors
- **Admin Dashboard** – Manage all users, donors, patients, and feedback
- **Blood Compatibility** – Smart donor matching based on blood group compatibility
- **JWT Authentication** – Secure token-based auth with role switching (donor/patient/both)

## Tech Stack
- **Frontend**: React, React Router, Axios, React Toastify
- **Backend**: Spring Boot 3.3, Spring Security, Spring Data JPA, JWT (jjwt)
- **Database**: H2 (development) / PostgreSQL (production)
- **Deployment**: Render.com

## Local Development

### Prerequisites
- Java 21+
- Node.js 18+
- Maven (or use the included `mvnw` wrapper)

### Backend Setup
```bash
cd backend
./mvnw spring-boot:run
```
The Spring Boot server runs on **http://localhost:8080** with an embedded H2 database.

- H2 Console: http://localhost:8080/h2-console

### Frontend Setup
```bash
cd frontend
npm install
npm start
```
The React dev server runs on **http://localhost:3000** and proxies API requests to Spring Boot on port 8080.

## Deployment on Render.com

1. Push this repo to GitHub
2. Go to [Render Dashboard](https://dashboard.render.com)
3. Click **New** → **Blueprint** and connect your repo
4. Render will auto-detect `render.yaml` and create the web service + PostgreSQL database
5. Deploy!

Or manually:
1. Create a **PostgreSQL** database on Render
2. Create a **Web Service** (Java runtime) pointing to your repo
3. Set Build Command: `./build.sh`
4. Set Start Command: `java -jar backend/target/bloodcircle-backend-1.0.0.jar`
5. Add environment variables: `SPRING_PROFILES_ACTIVE=prod`, `SPRING_DATASOURCE_URL`, `JWT_SECRET`

## Project Structure
```
BloodCircle/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/bloodcircle/
│       ├── BloodCircleApplication.java
│       ├── config/           # CORS, DataInitializer
│       ├── controller/       # Auth, Admin, Donor, Patient, Main
│       ├── model/            # JPA entities
│       ├── repository/       # Spring Data repos
│       ├── security/         # JWT filter, config
│       ├── exception/        # Error handling
│       └── util/             # Helpers
├── frontend/
│   ├── public/
│   └── src/
│       ├── api.js            # Axios instance
│       ├── context/          # Auth context
│       ├── components/       # Navbar, Footer, ProtectedRoute
│       └── pages/            # All page components
├── build.sh
├── render.yaml
└── .env.example
```

## Default Admin
- Email: `chiranjeevi.kola@zohomail.in`
- Password: `g0abdkbxa6`
