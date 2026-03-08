"""
Donor API endpoints.
"""
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from datetime import datetime, date
from app import db
from app.models import Donor, User

donor_api = Blueprint('donor_api', __name__)


def _get_donor_user():
    user = User.query.get(int(get_jwt_identity()))
    if not user:
        return None, None, (jsonify(error='User not found.'), 404)
    if user.role != 'donor':
        return user, None, (jsonify(error='Access denied. Donors only.'), 403)
    return user, user.donor, None


@donor_api.route('/register', methods=['POST'])
@jwt_required()
def register():
    user = User.query.get(int(get_jwt_identity()))
    if not user:
        return jsonify(error='User not found.'), 404

    data = request.get_json() or {}
    required = ('full_name', 'phone', 'blood_group', 'date_of_birth', 'gender', 'city', 'state', 'pincode')
    for f in required:
        if not data.get(f):
            return jsonify(error=f'{f} is required.'), 400

    try:
        dob = date.fromisoformat(data['date_of_birth'])
    except (ValueError, TypeError):
        return jsonify(error='Invalid date_of_birth format. Use YYYY-MM-DD.'), 400

    # Age validation
    today = date.today()
    age = today.year - dob.year - ((today.month, today.day) < (dob.month, dob.day))
    if age < 18:
        return jsonify(error='Must be at least 18 years old.'), 400
    if age > 65:
        return jsonify(error='Must be under 65 years old.'), 400

    last_donation = None
    if data.get('last_donation_date'):
        try:
            last_donation = date.fromisoformat(data['last_donation_date'])
        except (ValueError, TypeError):
            return jsonify(error='Invalid last_donation_date format.'), 400

    user.role = 'donor'

    if user.donor:
        donor = user.donor
        donor.full_name = data['full_name']
        donor.phone = data['phone']
        donor.blood_group = data['blood_group']
        donor.date_of_birth = dob
        donor.gender = data['gender']
        donor.city = data['city']
        donor.state = data['state']
        donor.pincode = data['pincode']
        donor.last_donation_date = last_donation
        donor.medical_history = data.get('medical_history', '')
        donor.is_available = data.get('is_available', True)
        donor.updated_at = datetime.utcnow()
    else:
        donor = Donor(
            user_id=user.id,
            full_name=data['full_name'],
            phone=data['phone'],
            blood_group=data['blood_group'],
            date_of_birth=dob,
            gender=data['gender'],
            city=data['city'],
            state=data['state'],
            pincode=data['pincode'],
            last_donation_date=last_donation,
            medical_history=data.get('medical_history', ''),
            is_available=data.get('is_available', True),
        )
        db.session.add(donor)

    db.session.commit()
    return jsonify(donor=donor.to_dict(), user=user.to_dict()), 201


@donor_api.route('/dashboard', methods=['GET'])
@jwt_required()
def dashboard():
    user, donor, err = _get_donor_user()
    if err:
        return err
    if not donor:
        return jsonify(error='Complete your donor profile first.', needs_profile=True), 404

    can_donate = donor.can_donate()
    days_since = None
    if donor.last_donation_date:
        days_since = (datetime.today().date() - donor.last_donation_date).days

    return jsonify(
        donor=donor.to_dict(),
        can_donate=can_donate,
        days_since_donation=days_since,
    )


@donor_api.route('/profile', methods=['GET'])
@jwt_required()
def profile():
    user, donor, err = _get_donor_user()
    if err:
        return err
    if not donor:
        return jsonify(error='Profile not found.', needs_profile=True), 404
    return jsonify(donor=donor.to_dict())


@donor_api.route('/profile', methods=['PUT'])
@jwt_required()
def update_profile():
    user, donor, err = _get_donor_user()
    if err:
        return err
    if not donor:
        return jsonify(error='Profile not found.'), 404

    data = request.get_json() or {}

    if data.get('full_name'):
        donor.full_name = data['full_name']
    if data.get('phone'):
        donor.phone = data['phone']
    if data.get('blood_group'):
        donor.blood_group = data['blood_group']
    if data.get('date_of_birth'):
        try:
            donor.date_of_birth = date.fromisoformat(data['date_of_birth'])
        except (ValueError, TypeError):
            return jsonify(error='Invalid date format.'), 400
    if data.get('gender'):
        donor.gender = data['gender']
    if data.get('city'):
        donor.city = data['city']
    if data.get('state'):
        donor.state = data['state']
    if data.get('pincode'):
        donor.pincode = data['pincode']
    if 'last_donation_date' in data:
        if data['last_donation_date']:
            try:
                donor.last_donation_date = date.fromisoformat(data['last_donation_date'])
            except (ValueError, TypeError):
                return jsonify(error='Invalid date format.'), 400
        else:
            donor.last_donation_date = None
    if 'medical_history' in data:
        donor.medical_history = data['medical_history']
    if 'is_available' in data:
        donor.is_available = bool(data['is_available'])

    donor.updated_at = datetime.utcnow()
    db.session.commit()
    return jsonify(donor=donor.to_dict())


@donor_api.route('/toggle-availability', methods=['POST'])
@jwt_required()
def toggle_availability():
    user, donor, err = _get_donor_user()
    if err:
        return err
    if not donor:
        return jsonify(error='Profile not found.'), 404

    donor.is_available = not donor.is_available
    donor.updated_at = datetime.utcnow()
    db.session.commit()
    return jsonify(donor=donor.to_dict(), message=f'Status: {"available" if donor.is_available else "unavailable"}')
