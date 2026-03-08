import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import API from '../../api';

export default function DonorProfile() {
  const [donor, setDonor] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    API.get('/donor/profile')
      .then(r => setDonor(r.data.donor))
      .catch(err => {
        if (err.response?.data?.needs_profile) navigate('/donor/register');
      });
  }, [navigate]);

  if (!donor) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: '700px' }}>
        <h1 className="section-title">My Donor Profile</h1>
        <div className="card">
          <div style={{ display: 'grid', gap: '0.75rem' }}>
            <p><strong>Full Name:</strong> {donor.full_name}</p>
            <p><strong>Email:</strong> {donor.email}</p>
            <p><strong>Phone:</strong> {donor.phone}</p>
            <p><strong>Blood Group:</strong> <span className="badge badge-blood">{donor.blood_group}</span></p>
            <p><strong>Gender:</strong> {donor.gender}</p>
            <p><strong>Age:</strong> {donor.age} years</p>
            <p><strong>City:</strong> {donor.city}</p>
            <p><strong>State:</strong> {donor.state}</p>
            <p><strong>Pincode:</strong> {donor.pincode}</p>
            <p><strong>Last Donation:</strong> {donor.last_donation_date || 'N/A'}</p>
            <p><strong>Can Donate:</strong> {donor.can_donate ? '✅ Yes' : '❌ Not yet (wait 90 days)'}</p>
            <p><strong>Available:</strong> {donor.is_available ? '✅ Available' : '❌ Unavailable'}</p>
            {donor.medical_history && <p><strong>Medical History:</strong> {donor.medical_history}</p>}
          </div>
          <div className="mt-2">
            <Link to="/donor/edit-profile" className="btn btn-primary">Edit Profile</Link>
            <Link to="/donor/dashboard" className="btn btn-outline" style={{ marginLeft: '0.5rem' }}>Back to Dashboard</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
