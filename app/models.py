"""
Database models for the BloodCircle application.
"""
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash
from app import db


class User(db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), unique=True, nullable=False, index=True)
    phone = db.Column(db.String(20), nullable=True)
    password_hash = db.Column(db.String(255), nullable=False)
    role = db.Column(db.String(20), nullable=True)
    is_verified = db.Column(db.Boolean, default=False)
    is_active = db.Column(db.Boolean, default=True)
    deleted_at = db.Column(db.DateTime, nullable=True)
    is_blocked = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    last_login = db.Column(db.DateTime)

    donor = db.relationship('Donor', backref='user', uselist=False, cascade='all, delete-orphan')
    patient = db.relationship('Patient', backref='user', uselist=False, cascade='all, delete-orphan')

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    def to_dict(self):
        return {
            'id': self.id,
            'email': self.email,
            'phone': self.phone,
            'role': self.role,
            'is_verified': self.is_verified,
            'is_active': self.is_active,
            'is_blocked': self.is_blocked,
            'is_deleted': self.deleted_at is not None,
            'is_admin': self.role == 'admin',
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'last_login': self.last_login.isoformat() if self.last_login else None,
            'has_donor_profile': self.donor is not None,
            'has_patient_profile': self.patient is not None,
        }


class Donor(db.Model):
    __tablename__ = 'donors'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False, unique=True)
    full_name = db.Column(db.String(100), nullable=False)
    phone = db.Column(db.String(20), nullable=False)
    blood_group = db.Column(db.String(5), nullable=False, index=True)
    date_of_birth = db.Column(db.Date, nullable=False)
    gender = db.Column(db.String(10), nullable=False)
    city = db.Column(db.String(50), nullable=False, index=True)
    state = db.Column(db.String(50), nullable=False)
    pincode = db.Column(db.String(10), nullable=False)
    last_donation_date = db.Column(db.Date)
    medical_history = db.Column(db.Text)
    is_available = db.Column(db.Boolean, default=True, index=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    def get_age(self):
        today = datetime.today().date()
        return today.year - self.date_of_birth.year - (
            (today.month, today.day) < (self.date_of_birth.month, self.date_of_birth.day)
        )

    def can_donate(self):
        if not self.last_donation_date:
            return True
        return (datetime.today().date() - self.last_donation_date).days >= 90

    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'full_name': self.full_name,
            'phone': self.phone,
            'blood_group': self.blood_group,
            'date_of_birth': self.date_of_birth.isoformat() if self.date_of_birth else None,
            'gender': self.gender,
            'city': self.city,
            'state': self.state,
            'pincode': self.pincode,
            'last_donation_date': self.last_donation_date.isoformat() if self.last_donation_date else None,
            'medical_history': self.medical_history,
            'is_available': self.is_available,
            'age': self.get_age(),
            'can_donate': self.can_donate(),
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'email': self.user.email if self.user else None,
        }


class Patient(db.Model):
    __tablename__ = 'patients'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False, unique=True)
    full_name = db.Column(db.String(100), nullable=False)
    phone = db.Column(db.String(20), nullable=False)
    blood_group_required = db.Column(db.String(5), nullable=False, index=True)
    hospital_name = db.Column(db.String(100), nullable=False)
    city = db.Column(db.String(50), nullable=False, index=True)
    state = db.Column(db.String(50), nullable=False)
    pincode = db.Column(db.String(10), nullable=False)
    urgency_level = db.Column(db.String(20), nullable=False)
    required_by_date = db.Column(db.Date, nullable=False)
    medical_condition = db.Column(db.Text)
    is_fulfilled = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    def days_remaining(self):
        return (self.required_by_date - datetime.today().date()).days

    def to_dict(self):
        return {
            'id': self.id,
            'user_id': self.user_id,
            'full_name': self.full_name,
            'phone': self.phone,
            'blood_group_required': self.blood_group_required,
            'hospital_name': self.hospital_name,
            'city': self.city,
            'state': self.state,
            'pincode': self.pincode,
            'urgency_level': self.urgency_level,
            'required_by_date': self.required_by_date.isoformat() if self.required_by_date else None,
            'medical_condition': self.medical_condition,
            'is_fulfilled': self.is_fulfilled,
            'days_remaining': self.days_remaining(),
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'email': self.user.email if self.user else None,
        }


class Feedback(db.Model):
    __tablename__ = 'feedback'

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(120), nullable=False)
    subject = db.Column(db.String(200), nullable=False)
    message = db.Column(db.Text, nullable=False)
    rating = db.Column(db.Integer)
    is_resolved = db.Column(db.Boolean, default=False)
    admin_response = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    resolved_at = db.Column(db.DateTime)

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'email': self.email,
            'subject': self.subject,
            'message': self.message,
            'rating': self.rating,
            'is_resolved': self.is_resolved,
            'is_read': self.is_resolved,
            'admin_response': self.admin_response,
            'created_at': self.created_at.isoformat() if self.created_at else None,
            'resolved_at': self.resolved_at.isoformat() if self.resolved_at else None,
        }


# Blood compatibility mapping
BLOOD_COMPATIBILITY = {
    'O-': ['O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'],
    'O+': ['O+', 'A+', 'B+', 'AB+'],
    'A-': ['A-', 'A+', 'AB-', 'AB+'],
    'A+': ['A+', 'AB+'],
    'B-': ['B-', 'B+', 'AB-', 'AB+'],
    'B+': ['B+', 'AB+'],
    'AB-': ['AB-', 'AB+'],
    'AB+': ['AB+'],
}


def get_compatible_blood_groups(blood_group_required):
    compatible = []
    for donor_group, can_donate_to in BLOOD_COMPATIBILITY.items():
        if blood_group_required in can_donate_to:
            compatible.append(donor_group)
    return compatible
