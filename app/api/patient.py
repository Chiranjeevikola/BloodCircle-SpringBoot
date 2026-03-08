"""
Patient API endpoints.
"""
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from datetime import datetime, date
from app import db
from app.models import Patient, Donor, User, get_compatible_blood_groups

patient_api = Blueprint('patient_api', __name__)


def _get_patient_user():
    user = User.query.get(int(get_jwt_identity()))
    if not user:
        return None, None, (jsonify(error='User not found.'), 404)
    if user.role != 'patient':
        return user, None, (jsonify(error='Access denied. Patients only.'), 403)
    return user, user.patient, None


@patient_api.route('/register', methods=['POST'])
@jwt_required()
def register():
    user = User.query.get(int(get_jwt_identity()))
    if not user:
        return jsonify(error='User not found.'), 404

    data = request.get_json() or {}
    required = ('full_name', 'phone', 'blood_group_required', 'hospital_name',
                'city', 'state', 'pincode', 'urgency_level', 'required_by_date')
    for f in required:
        if not data.get(f):
            return jsonify(error=f'{f} is required.'), 400

    try:
        req_date = date.fromisoformat(data['required_by_date'])
    except (ValueError, TypeError):
        return jsonify(error='Invalid required_by_date format. Use YYYY-MM-DD.'), 400

    user.role = 'patient'

    if user.patient:
        p = user.patient
        p.full_name = data['full_name']
        p.phone = data['phone']
        p.blood_group_required = data['blood_group_required']
        p.hospital_name = data['hospital_name']
        p.city = data['city']
        p.state = data['state']
        p.pincode = data['pincode']
        p.urgency_level = data['urgency_level']
        p.required_by_date = req_date
        p.medical_condition = data.get('medical_condition', '')
        p.updated_at = datetime.utcnow()
    else:
        p = Patient(
            user_id=user.id,
            full_name=data['full_name'],
            phone=data['phone'],
            blood_group_required=data['blood_group_required'],
            hospital_name=data['hospital_name'],
            city=data['city'],
            state=data['state'],
            pincode=data['pincode'],
            urgency_level=data['urgency_level'],
            required_by_date=req_date,
            medical_condition=data.get('medical_condition', ''),
        )
        db.session.add(p)

    db.session.commit()
    return jsonify(patient=p.to_dict(), user=user.to_dict()), 201


@patient_api.route('/dashboard', methods=['GET'])
@jwt_required()
def dashboard():
    user, patient, err = _get_patient_user()
    if err:
        return err
    if not patient:
        return jsonify(error='Complete your patient profile first.', needs_profile=True), 404

    compatible = get_compatible_blood_groups(patient.blood_group_required)
    matching = Donor.query.filter(
        Donor.blood_group.in_(compatible),
        Donor.is_available == True,
        Donor.city.ilike(f'%{patient.city}%'),
    ).limit(10).all()

    return jsonify(
        patient=patient.to_dict(),
        matching_donors=[d.to_dict() for d in matching],
        compatible_groups=compatible,
    )


@patient_api.route('/profile', methods=['GET'])
@jwt_required()
def profile():
    user, patient, err = _get_patient_user()
    if err:
        return err
    if not patient:
        return jsonify(error='Profile not found.', needs_profile=True), 404

    compatible = get_compatible_blood_groups(patient.blood_group_required)
    return jsonify(patient=patient.to_dict(), compatible_groups=compatible)


@patient_api.route('/profile', methods=['PUT'])
@jwt_required()
def update_profile():
    user, patient, err = _get_patient_user()
    if err:
        return err
    if not patient:
        return jsonify(error='Profile not found.'), 404

    data = request.get_json() or {}

    if data.get('full_name'):
        patient.full_name = data['full_name']
    if data.get('phone'):
        patient.phone = data['phone']
    if data.get('blood_group_required'):
        patient.blood_group_required = data['blood_group_required']
    if data.get('hospital_name'):
        patient.hospital_name = data['hospital_name']
    if data.get('city'):
        patient.city = data['city']
    if data.get('state'):
        patient.state = data['state']
    if data.get('pincode'):
        patient.pincode = data['pincode']
    if data.get('urgency_level'):
        patient.urgency_level = data['urgency_level']
    if data.get('required_by_date'):
        try:
            patient.required_by_date = date.fromisoformat(data['required_by_date'])
        except (ValueError, TypeError):
            return jsonify(error='Invalid date format.'), 400
    if 'medical_condition' in data:
        patient.medical_condition = data['medical_condition']
    if 'is_fulfilled' in data:
        patient.is_fulfilled = bool(data['is_fulfilled'])

    patient.updated_at = datetime.utcnow()
    db.session.commit()
    return jsonify(patient=patient.to_dict())


@patient_api.route('/search', methods=['GET'])
@jwt_required()
def search_donors():
    user, patient, err = _get_patient_user()
    if err:
        return err
    if not patient:
        return jsonify(error='Complete profile first.'), 404

    blood_group = request.args.get('blood_group', '')
    city = request.args.get('city', '')
    state = request.args.get('state', '')
    available_only = request.args.get('available_only', 'true').lower() == 'true'
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 10, type=int)

    compatible = get_compatible_blood_groups(patient.blood_group_required)
    query = Donor.query

    if blood_group:
        query = query.filter(Donor.blood_group == blood_group)
    else:
        query = query.filter(Donor.blood_group.in_(compatible))

    if city:
        query = query.filter(Donor.city.ilike(f'%{city}%'))
    if state:
        query = query.filter(Donor.state.ilike(f'%{state}%'))
    if available_only:
        query = query.filter(Donor.is_available == True)

    pagination = query.order_by(Donor.created_at.desc()).paginate(page=page, per_page=per_page, error_out=False)

    return jsonify(
        donors=[d.to_dict() for d in pagination.items],
        total=pagination.total,
        pages=pagination.pages,
        page=pagination.page,
        compatible_groups=compatible,
    )


@patient_api.route('/donor/<int:donor_id>', methods=['GET'])
@jwt_required()
def view_donor(donor_id):
    user, patient, err = _get_patient_user()
    if err:
        return err
    if not patient:
        return jsonify(error='Complete profile first.'), 404

    donor = Donor.query.get_or_404(donor_id)
    compatible = get_compatible_blood_groups(patient.blood_group_required)

    return jsonify(
        donor=donor.to_dict(),
        is_compatible=donor.blood_group in compatible,
    )
