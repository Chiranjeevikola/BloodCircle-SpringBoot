"""
Flask application factory — serves React build in production and provides REST API.
"""
import os
from flask import Flask, send_from_directory, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from flask_jwt_extended import JWTManager
from config import config

db = SQLAlchemy()
jwt = JWTManager()


def create_app(config_name='default'):
    app = Flask(__name__, static_folder=None)
    app.config.from_object(config[config_name])

    db.init_app(app)
    jwt.init_app(app)
    CORS(app, supports_credentials=True)

    # ---- JWT error handlers ------------------------------------------------
    @jwt.expired_token_loader
    def expired_token_callback(jwt_header, jwt_payload):
        return jsonify({"error": "Token has expired", "code": "token_expired"}), 401

    @jwt.invalid_token_loader
    def invalid_token_callback(error):
        return jsonify({"error": "Invalid token", "code": "invalid_token"}), 401

    @jwt.unauthorized_loader
    def missing_token_callback(error):
        return jsonify({"error": "Authorization required", "code": "authorization_required"}), 401

    # ---- Register API blueprints -------------------------------------------
    from app.api.auth import auth_api
    from app.api.main import main_api
    from app.api.donor import donor_api
    from app.api.patient import patient_api
    from app.api.admin import admin_api

    app.register_blueprint(auth_api, url_prefix='/api/auth')
    app.register_blueprint(main_api, url_prefix='/api')
    app.register_blueprint(donor_api, url_prefix='/api/donor')
    app.register_blueprint(patient_api, url_prefix='/api/patient')
    app.register_blueprint(admin_api, url_prefix='/api/admin')

    # ---- Serve React build in production -----------------------------------
    react_build = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'frontend', 'build')

    if os.path.isdir(react_build):
        @app.route('/', defaults={'path': ''})
        @app.route('/<path:path>')
        def serve_react(path):
            # Don't intercept API routes
            if path.startswith('api/'):
                return jsonify(error='Not found'), 404
            full = os.path.join(react_build, path)
            if path and os.path.isfile(full):
                return send_from_directory(react_build, path)
            return send_from_directory(react_build, 'index.html')

    # ---- Create tables on first request ------------------------------------
    with app.app_context():
        try:
            db.create_all()
        except Exception as e:
            print(f"DB init note: {e}")

    return app


from app import models
