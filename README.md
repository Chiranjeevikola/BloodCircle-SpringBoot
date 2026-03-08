# BloodCircle

A full-stack blood donation management platform built with **React** frontend and **Flask** REST API backend.

## Features
- **Donor Management** – Register, manage profile, toggle availability
- **Patient Management** – Register, manage requests, search compatible donors
- **Admin Dashboard** – Manage all users, donors, patients, and feedback
- **Blood Compatibility** – Smart donor matching based on blood group compatibility
- **JWT Authentication** – Secure token-based auth with role switching (donor/patient/both)

## Tech Stack
- **Frontend**: React, React Router, Axios, React Toastify
- **Backend**: Flask, Flask-JWT-Extended, Flask-CORS, SQLAlchemy, Flask-Migrate
- **Database**: PostgreSQL
- **Deployment**: Render.com

## Local Development

### Prerequisites
- Python 3.11+
- Node.js 18+
- PostgreSQL

### Backend Setup
```bash
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Set environment variables
export DATABASE_URL=postgresql://user:pass@localhost/bloodcircle
export SECRET_KEY=dev-secret
export JWT_SECRET_KEY=dev-jwt-secret

# Initialize database
flask init-db

# Create admin user
python init_admin.py

# Run backend
flask run
```

### Frontend Setup
```bash
cd frontend
npm install
npm start
```

The React dev server runs on port 3000 and proxies API requests to Flask on port 5000.

## Deployment on Render.com

1. Push this repo to GitHub
2. Go to [Render Dashboard](https://dashboard.render.com)
3. Click **New** → **Blueprint** and connect your repo
4. Render will auto-detect `render.yaml` and create the web service + database
5. Deploy!

Or manually:
1. Create a **PostgreSQL** database on Render
2. Create a **Web Service** pointing to your repo
3. Set Build Command: `./build.sh`
4. Set Start Command: `gunicorn run:app`
5. Add environment variables: `DATABASE_URL`, `SECRET_KEY`, `JWT_SECRET_KEY`

## Project Structure
```
BloodCircle/
├── app/
│   ├── __init__.py          # Flask factory + React serving
│   ├── models.py            # SQLAlchemy models
│   └── api/
│       ├── auth.py          # Auth endpoints (JWT)
│       ├── main.py          # Public endpoints
│       ├── donor.py         # Donor CRUD
│       ├── patient.py       # Patient CRUD + search
│       └── admin.py         # Admin management
├── frontend/
│   ├── public/
│   └── src/
│       ├── api.js           # Axios instance
│       ├── context/         # Auth context
│       ├── components/      # Navbar, Footer, ProtectedRoute
│       └── pages/           # All page components
├── config.py
├── run.py
├── requirements.txt
├── build.sh
├── render.yaml
└── runtime.txt
```

## Default Admin
- Email: `chiranjeevi.kola@zohomail.in`
- Password: `g0abdkbxa6`
