#!/usr/bin/env bash
set -o errexit

# Install Python dependencies
pip install -r requirements.txt

# Build React frontend
cd frontend
npm install
npm run build
cd ..

# Initialize database and admin user
python -c "from app import create_app, db; app = create_app(); app.app_context().push(); db.create_all()"
python init_admin.py
