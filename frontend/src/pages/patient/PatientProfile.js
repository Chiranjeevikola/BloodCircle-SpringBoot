import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import API from '../../api';

export default function PatientProfile() {
  const [patient, setPatient] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    API.get('/patient/profile').then(r => setPatient(r.data.patient)).catch(() => navigate('/patient/register'));
  }, [navigate]);

  if (!patient) return <div className="loading">Loading...</div>;

  const urgencyClass = patient.urgency_level === 'Critical' ? 'badge-danger'
    : patient.urgency_level === 'Urgent' ? 'badge-warning' : 'badge-primary';

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: '700px' }}>
        <h1 className="section-title text-center">Patient Profile</h1>
        <div className="card">
          <div className="profile-info">
            <div className="profile-row"><strong>Name:</strong> {patient.full_name}</div>
            <div className="profile-row"><strong>Phone:</strong> {patient.phone}</div>
            <div className="profile-row"><strong>Blood Group:</strong> <span className="badge badge-danger">{patient.blood_group}</span></div>
            <div className="profile-row"><strong>Gender:</strong> {patient.gender}</div>
            <div className="profile-row"><strong>Date of Birth:</strong> {patient.date_of_birth || 'N/A'}</div>
            <div className="profile-row"><strong>City:</strong> {patient.city}</div>
            <div className="profile-row"><strong>State:</strong> {patient.state}</div>
            <div className="profile-row"><strong>Pincode:</strong> {patient.pincode}</div>
            <div className="profile-row"><strong>Urgency:</strong> <span className={`badge ${urgencyClass}`}>{patient.urgency_level}</span></div>
            <div className="profile-row"><strong>Medical Condition:</strong> {patient.medical_condition || 'Not specified'}</div>
            <div className="profile-row"><strong>Status:</strong> {patient.is_fulfilled ? <span className="badge badge-success">Fulfilled</span> : <span className="badge badge-warning">Active</span>}</div>
            <div className="profile-row"><strong>Registered:</strong> {new Date(patient.created_at).toLocaleDateString()}</div>
          </div>
          <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem', flexWrap: 'wrap' }}>
            <Link to="/patient/edit-profile" className="btn btn-primary">Edit Profile</Link>
            <Link to="/patient/search" className="btn btn-outline">Search Donors</Link>
            <Link to="/patient/dashboard" className="btn btn-secondary">Dashboard</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
