import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import API from '../../api';
import { FaPhone, FaTint, FaMapMarkerAlt } from 'react-icons/fa';

export default function ViewDonor() {
  const { id } = useParams();
  const [donor, setDonor] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    API.get(`/patient/donor/${id}`).then(r => setDonor(r.data.donor)).catch(() => navigate('/patient/search'));
  }, [id, navigate]);

  if (!donor) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: '700px' }}>
        <h1 className="section-title text-center">Donor Details</h1>
        <div className="card">
          <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
            <FaTint style={{ fontSize: '3rem', color: 'var(--primary)' }} />
            <h2>{donor.full_name}</h2>
            <span className="badge badge-danger" style={{ fontSize: '1.2rem' }}>{donor.blood_group}</span>
            {donor.is_available ? (
              <span className="badge badge-success" style={{ marginLeft: '0.5rem' }}>Available</span>
            ) : (
              <span className="badge badge-secondary" style={{ marginLeft: '0.5rem' }}>Unavailable</span>
            )}
          </div>

          <div className="profile-info">
            <div className="profile-row"><FaPhone /> <strong>Phone:</strong> {donor.phone}</div>
            <div className="profile-row"><strong>Gender:</strong> {donor.gender}</div>
            <div className="profile-row"><strong>Age:</strong> {donor.age}</div>
            <div className="profile-row"><FaMapMarkerAlt /> <strong>Location:</strong> {donor.city}, {donor.state} - {donor.pincode}</div>
            {donor.last_donation_date && (
              <div className="profile-row"><strong>Last Donation:</strong> {donor.last_donation_date}</div>
            )}
          </div>

          <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem', flexWrap: 'wrap' }}>
            <a href={`tel:${donor.phone}`} className="btn btn-primary"><FaPhone /> Call Donor</a>
            <Link to="/patient/search" className="btn btn-outline">Back to Search</Link>
            <Link to="/patient/dashboard" className="btn btn-secondary">Dashboard</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
