"""
Main / public API endpoints.
"""
from flask import Blueprint, request, jsonify
from app import db
from app.models import Donor, Patient, Feedback, BLOOD_COMPATIBILITY

main_api = Blueprint('main_api', __name__)


@main_api.route('/stats', methods=['GET'])
def stats():
    total_donors = Donor.query.count()
    available_donors = Donor.query.filter_by(is_available=True).count()
    total_patients = Patient.query.count()
    return jsonify(
        total_donors=total_donors,
        available_donors=available_donors,
        total_patients=total_patients,
    )


@main_api.route('/feedback', methods=['POST'])
def submit_feedback():
    data = request.get_json() or {}
    required = ('name', 'email', 'subject', 'message')
    for field in required:
        if not data.get(field):
            return jsonify(error=f'{field} is required.'), 400

    fb = Feedback(
        name=data['name'],
        email=data['email'],
        subject=data['subject'],
        message=data['message'],
        rating=int(data['rating']) if data.get('rating') else None,
    )
    db.session.add(fb)
    db.session.commit()
    return jsonify(message='Feedback submitted successfully!'), 201


@main_api.route('/blood-compatibility', methods=['GET'])
def blood_compatibility():
    compat = {}
    receive_from = {
        'O-': ['O-'],
        'O+': ['O-', 'O+'],
        'A-': ['A-', 'O-'],
        'A+': ['A-', 'A+', 'O-', 'O+'],
        'B-': ['B-', 'O-'],
        'B+': ['B-', 'B+', 'O-', 'O+'],
        'AB-': ['AB-', 'A-', 'B-', 'O-'],
        'AB+': ['O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'],
    }
    for bg, donate_to in BLOOD_COMPATIBILITY.items():
        compat[bg] = {
            'can_donate_to': donate_to,
            'can_receive_from': receive_from.get(bg, []),
        }
    return jsonify(compatibility=compat)


@main_api.route('/health', methods=['GET'])
def health():
    return jsonify(status='ok')
