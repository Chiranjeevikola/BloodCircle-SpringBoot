"""
Initialize database and create default admin user for deployment.
"""
import os
import sys
from app import create_app, db
from app.models import User


def init_admin():
    config_name = os.environ.get('FLASK_ENV', 'development')
    app = create_app(config_name)

    with app.app_context():
        try:
            db.create_all()
            print('Database tables ready.')

            admin_email = 'chiranjeevi.kola@zohomail.in'
            admin_password = 'g0abdkbxa6'
            existing = User.query.filter_by(email=admin_email).first()

            if existing:
                if existing.role != 'admin':
                    existing.role = 'admin'
                    db.session.commit()
                print(f'Admin exists: {admin_email}')
            else:
                admin = User(email=admin_email, role='admin', is_verified=True, is_active=True, is_blocked=False)
                admin.set_password(admin_password)
                db.session.add(admin)
                db.session.commit()
                print(f'Admin created: {admin_email}')

            from app.models import Donor, Patient
            print(f'Users: {User.query.count()}, Donors: {Donor.query.count()}, Patients: {Patient.query.count()}')
            return True
        except Exception as e:
            print(f'Error: {e}', file=sys.stderr)
            import traceback
            traceback.print_exc()
            return False


if __name__ == '__main__':
    success = init_admin()
    sys.exit(0 if success else 1)
