"""
Admin API endpoints.
"""
from flask import Blueprint, request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from datetime import datetime, timedelta
from sqlalchemy import func
from app import db
from app.models import User, Donor, Patient, Feedback
from functools import wraps

admin_api = Blueprint('admin_api', __name__)


def admin_required(f):
    @wraps(f)
    @jwt_required()
    def decorated(*args, **kwargs):
        user = User.query.get(int(get_jwt_identity()))
        if not user or user.role != 'admin':
            return jsonify(error='Admin access required.'), 403
        return f(*args, **kwargs)
    return decorated


@admin_api.route('/dashboard', methods=['GET'])
@admin_required
def dashboard():
    week_ago = datetime.utcnow() - timedelta(days=7)
    recent_users = User.query.filter(User.created_at >= week_ago).count()

    total_users = User.query.count()
    total_donors = Donor.query.count()
    total_patients = Patient.query.count()
    active_donors = Donor.query.filter_by(is_available=True).count()
    active_patients = Patient.query.filter_by(is_fulfilled=False).count()
    blocked_users = User.query.filter_by(is_blocked=True).count()
    total_feedback = Feedback.query.count()
    unread_feedback = Feedback.query.filter_by(is_resolved=False).count()

    blood_groups = ['A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-']

    donor_dist = dict(db.session.query(Donor.blood_group, func.count(Donor.id)).group_by(Donor.blood_group).all())
    patient_dist = dict(db.session.query(Patient.blood_group_required, func.count(Patient.id)).group_by(Patient.blood_group_required).all())

    donor_counts = [donor_dist.get(bg, 0) for bg in blood_groups]
    patient_counts = [patient_dist.get(bg, 0) for bg in blood_groups]

    urgent = Patient.query.filter(Patient.urgency_level == 'Critical', Patient.is_fulfilled == False).limit(5).all()
    recent_fb = Feedback.query.order_by(Feedback.created_at.desc()).limit(5).all()

    blood_group_stats = {bg: donor_dist.get(bg, 0) for bg in blood_groups}

    return jsonify(
        total_users=total_users,
        recent_users=recent_users,
        total_donors=total_donors,
        total_patients=total_patients,
        active_donors=active_donors,
        active_patients=active_patients,
        blocked_users=blocked_users,
        total_feedback=total_feedback,
        unread_feedback=unread_feedback,
        blood_groups=blood_groups,
        blood_group_stats=blood_group_stats,
        donor_counts=donor_counts,
        patient_counts=patient_counts,
        urgent_patients=[p.to_dict() for p in urgent],
        recent_feedback=[f.to_dict() for f in recent_fb],
    )


@admin_api.route('/users', methods=['GET'])
@admin_required
def manage_users():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 20, type=int)
    role = request.args.get('role', 'all')
    status = request.args.get('status', 'all')
    search = request.args.get('search', '')

    query = User.query.filter(User.deleted_at.is_(None))
    if role != 'all':
        query = query.filter_by(role=role)
    if status == 'active':
        query = query.filter_by(is_active=True)
    elif status == 'inactive':
        query = query.filter_by(is_active=False)
    if search:
        query = query.filter(User.email.ilike(f'%{search}%'))

    pagination = query.order_by(User.created_at.desc()).paginate(page=page, per_page=per_page, error_out=False)

    users_data = []
    for u in pagination.items:
        d = u.to_dict()
        if u.donor:
            d['donor_name'] = u.donor.full_name
            d['donor_blood_group'] = u.donor.blood_group
        if u.patient:
            d['patient_name'] = u.patient.full_name
            d['patient_blood_group'] = u.patient.blood_group_required
        users_data.append(d)

    return jsonify(users=users_data, pagination=dict(total=pagination.total, pages=pagination.pages, page=pagination.page))


@admin_api.route('/donors', methods=['GET'])
@admin_required
def manage_donors():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 20, type=int)
    blood_group = request.args.get('blood_group', 'all')
    availability = request.args.get('availability', 'all')
    search = request.args.get('search', '')

    query = Donor.query
    if blood_group != 'all':
        query = query.filter_by(blood_group=blood_group)
    if availability == 'available':
        query = query.filter_by(is_available=True)
    elif availability == 'unavailable':
        query = query.filter_by(is_available=False)
    if search:
        query = query.filter(Donor.full_name.ilike(f'%{search}%'))

    pagination = query.order_by(Donor.created_at.desc()).paginate(page=page, per_page=per_page, error_out=False)
    return jsonify(donors=[d.to_dict() for d in pagination.items], pagination=dict(total=pagination.total, pages=pagination.pages, page=pagination.page))


@admin_api.route('/patients', methods=['GET'])
@admin_required
def manage_patients():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 20, type=int)
    blood_group = request.args.get('blood_group', 'all')
    urgency = request.args.get('urgency', 'all')
    fulfillment = request.args.get('fulfillment', 'all')
    search = request.args.get('search', '')

    query = Patient.query
    if blood_group != 'all':
        query = query.filter_by(blood_group_required=blood_group)
    if urgency != 'all':
        query = query.filter_by(urgency_level=urgency)
    if fulfillment == 'fulfilled':
        query = query.filter_by(is_fulfilled=True)
    elif fulfillment == 'pending':
        query = query.filter_by(is_fulfilled=False)
    if search:
        query = query.filter(Patient.full_name.ilike(f'%{search}%'))

    pagination = query.order_by(Patient.created_at.desc()).paginate(page=page, per_page=per_page, error_out=False)
    return jsonify(patients=[p.to_dict() for p in pagination.items], pagination=dict(total=pagination.total, pages=pagination.pages, page=pagination.page))


@admin_api.route('/feedback', methods=['GET'])
@admin_required
def manage_feedback():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 20, type=int)
    status = request.args.get('status', 'all')

    query = Feedback.query
    if status == 'pending':
        query = query.filter_by(is_resolved=False)
    elif status == 'resolved':
        query = query.filter_by(is_resolved=True)

    pagination = query.order_by(Feedback.created_at.desc()).paginate(page=page, per_page=per_page, error_out=False)
    return jsonify(feedbacks=[f.to_dict() for f in pagination.items], pagination=dict(total=pagination.total, pages=pagination.pages, page=pagination.page))


@admin_api.route('/user/<int:user_id>/toggle', methods=['POST'])
@admin_required
def toggle_user(user_id):
    user = User.query.get_or_404(user_id)
    if user.role == 'admin':
        return jsonify(error='Cannot modify admin accounts.'), 403
    user.is_active = not user.is_active
    db.session.commit()
    return jsonify(user=user.to_dict(), message=f'User {"activated" if user.is_active else "deactivated"}.')


@admin_api.route('/user/<int:user_id>/block', methods=['POST'])
@admin_required
def block_user(user_id):
    user = User.query.get_or_404(user_id)
    if user.role == 'admin':
        return jsonify(error='Cannot block admin.'), 403
    user.is_blocked = True
    user.is_active = False
    db.session.commit()
    return jsonify(user=user.to_dict(), message='User blocked.')


@admin_api.route('/user/<int:user_id>/unblock', methods=['POST'])
@admin_required
def unblock_user(user_id):
    user = User.query.get_or_404(user_id)
    user.is_blocked = False
    user.is_active = True
    db.session.commit()
    return jsonify(user=user.to_dict(), message='User unblocked.')


@admin_api.route('/user/<int:user_id>', methods=['DELETE'])
@admin_required
def delete_user(user_id):
    admin = User.query.get(int(get_jwt_identity()))
    user = User.query.get_or_404(user_id)
    if user.role == 'admin':
        return jsonify(error='Cannot delete admin.'), 403
    if user.id == admin.id:
        return jsonify(error='Cannot delete yourself.'), 403

    user.deleted_at = datetime.utcnow()
    user.is_active = False
    if user.donor:
        user.donor.is_available = False
    db.session.commit()
    return jsonify(message=f'User {user.email} deleted.')


@admin_api.route('/user/<int:user_id>', methods=['PUT'])
@admin_required
def edit_user(user_id):
    user = User.query.get_or_404(user_id)
    data = request.get_json() or {}

    if 'email' in data:
        user.email = data['email']
    if 'phone' in data:
        user.phone = data['phone']
    if 'role' in data:
        user.role = data['role']
    if 'is_active' in data:
        user.is_active = data['is_active']
    if 'is_verified' in data:
        user.is_verified = data['is_verified']
    if 'is_blocked' in data:
        user.is_blocked = data['is_blocked']

    db.session.commit()
    return jsonify(user=user.to_dict(), message='User updated.')


@admin_api.route('/feedback/<int:fid>/respond', methods=['POST'])
@admin_required
def respond_feedback(fid):
    fb = Feedback.query.get_or_404(fid)
    data = request.get_json() or {}
    response = data.get('response', '') or data.get('admin_response', '')
    if not response:
        return jsonify(error='Response is required.'), 400
    fb.admin_response = response
    fb.is_resolved = True
    fb.resolved_at = datetime.utcnow()
    db.session.commit()
    return jsonify(feedback=fb.to_dict(), message='Response sent.')


@admin_api.route('/feedback/<int:fid>/toggle', methods=['POST'])
@admin_required
def toggle_feedback(fid):
    fb = Feedback.query.get_or_404(fid)
    fb.is_resolved = not fb.is_resolved
    fb.resolved_at = datetime.utcnow() if fb.is_resolved else None
    db.session.commit()
    return jsonify(feedback=fb.to_dict())


@admin_api.route('/patient/<int:pid>/fulfill', methods=['POST'])
@admin_required
def fulfill_patient(pid):
    p = Patient.query.get_or_404(pid)
    p.is_fulfilled = True
    p.updated_at = datetime.utcnow()
    db.session.commit()
    return jsonify(patient=p.to_dict(), message='Request fulfilled.')
