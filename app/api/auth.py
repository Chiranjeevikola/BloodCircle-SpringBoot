"""
Authentication API endpoints.
"""
from flask import Blueprint, request, jsonify
from flask_jwt_extended import (
    create_access_token, create_refresh_token,
    jwt_required, get_jwt_identity
)
from datetime import datetime
from app import db
from app.models import User

auth_api = Blueprint('auth_api', __name__)


@auth_api.route('/register', methods=['POST'])
def register():
    data = request.get_json() or {}
    email = (data.get('email') or '').lower().strip()
    password = data.get('password', '')
    confirm = data.get('confirm_password', '')

    if not email or not password:
        return jsonify(error='Email and password are required.'), 400
    if len(password) < 6:
        return jsonify(error='Password must be at least 6 characters.'), 400
    if password != confirm:
        return jsonify(error='Passwords do not match.'), 400
    if User.query.filter_by(email=email).first():
        return jsonify(error='Email already registered.'), 409

    user = User(email=email, role=None, is_verified=True, is_active=True)
    user.set_password(password)
    db.session.add(user)
    db.session.commit()

    token = create_access_token(identity=str(user.id))
    refresh = create_refresh_token(identity=str(user.id))
    return jsonify(access_token=token, refresh_token=refresh, user=user.to_dict()), 201


@auth_api.route('/login', methods=['POST'])
def login():
    data = request.get_json() or {}
    email = (data.get('email') or '').lower().strip()
    password = data.get('password', '')

    user = User.query.filter_by(email=email).first()
    if not user or not user.check_password(password):
        return jsonify(error='Invalid email or password.'), 401
    if user.is_blocked:
        return jsonify(error='Your account has been blocked. Contact support.'), 403
    if user.deleted_at:
        days = (datetime.utcnow() - user.deleted_at).days
        if days <= 30:
            return jsonify(error='Account deleted. You can recover it within 30 days.', recoverable=True), 403
        return jsonify(error='Account permanently deleted.'), 403
    if not user.is_active:
        return jsonify(error='Account deactivated. Contact support.'), 403

    user.last_login = datetime.utcnow()
    db.session.commit()

    token = create_access_token(identity=str(user.id))
    refresh = create_refresh_token(identity=str(user.id))
    return jsonify(access_token=token, refresh_token=refresh, user=user.to_dict())


@auth_api.route('/me', methods=['GET'])
@jwt_required()
def me():
    user = User.query.get(int(get_jwt_identity()))
    if not user:
        return jsonify(error='User not found.'), 404
    data = user.to_dict()
    if user.donor:
        data['donor'] = user.donor.to_dict()
    if user.patient:
        data['patient'] = user.patient.to_dict()
    return jsonify(user=data)


@auth_api.route('/refresh', methods=['POST'])
@jwt_required(refresh=True)
def refresh():
    identity = get_jwt_identity()
    token = create_access_token(identity=identity)
    return jsonify(access_token=token)


@auth_api.route('/select-role', methods=['POST'])
@jwt_required()
def select_role():
    data = request.get_json() or {}
    role = data.get('role')
    if role not in ('donor', 'patient'):
        return jsonify(error='Role must be donor or patient.'), 400

    user = User.query.get(int(get_jwt_identity()))
    if not user:
        return jsonify(error='User not found.'), 404
    if user.role in ('admin', 'sub_admin'):
        return jsonify(error='Admins cannot change role.'), 403

    user.role = role
    db.session.commit()
    return jsonify(user=user.to_dict())


@auth_api.route('/delete-account', methods=['POST'])
@jwt_required()
def delete_account():
    user = User.query.get(int(get_jwt_identity()))
    if not user:
        return jsonify(error='User not found.'), 404
    user.deleted_at = datetime.utcnow()
    user.is_active = False
    if user.donor:
        user.donor.is_available = False
    db.session.commit()
    return jsonify(message='Account deleted. Recover within 30 days by logging in.')


@auth_api.route('/recover-account', methods=['POST'])
def recover_account():
    data = request.get_json() or {}
    email = (data.get('email') or '').lower().strip()
    password = data.get('password', '')

    user = User.query.filter_by(email=email).first()
    if not user or not user.check_password(password):
        return jsonify(error='Invalid credentials.'), 401
    if not user.deleted_at:
        return jsonify(error='Account is not deleted.'), 400
    if (datetime.utcnow() - user.deleted_at).days > 30:
        return jsonify(error='Recovery period expired.'), 410

    user.deleted_at = None
    user.is_active = True
    db.session.commit()
    return jsonify(message='Account recovered successfully.')
