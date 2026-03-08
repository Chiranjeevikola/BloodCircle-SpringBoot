import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import API from '../../api';
import { toast } from 'react-toastify';
import { FaTint, FaMapMarkerAlt, FaPhone, FaCalendarAlt } from 'react-icons/fa';

export default function DonorDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    API.get('/donor/dashboard')
      .then(r => setData(r.data))
      .catch(err => {
        if (err.response?.data?.needs_profile) navigate('/donor/register');
        else toast.error('Failed to load dashboard.');
      })
      .finally(() => setLoading(false));
  }, [navigate]);

  const toggleAvailability = async () => {
    try {
      const r = await API.post('/donor/toggle-availability');
      setData(prev => ({ ...prev, donor: r.data.donor }));
      toast.success(r.data.message);
    } catch {
      toast.error('Failed to toggle availability.');
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!data) return null;

  const { donor, can_donate, days_since_donation } = data;

  return (
    <div className="page">
      <div className="container">
        <div className="flex justify-between items-center flex-wrap mb-3" style={{ gap: '1rem' }}>
          <h1 className="section-title" style={{ marginBottom: 0 }}>Donor Dashboard</h1>
          <div className="flex gap-1">
            <Link to="/donor/profile" className="btn btn-outline btn-sm">View Profile</Link>
            <Link to="/donor/edit-profile" className="btn btn-outline btn-sm">Edit Profile</Link>
            <button onClick={toggleAvailability} className={`btn btn-sm ${donor.is_available ? 'btn-danger' : 'btn-success'}`}>
              {donor.is_available ? 'Set Unavailable' : 'Set Available'}
            </button>
          </div>
        </div>

        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-value"><span className="badge badge-blood">{donor.blood_group}</span></div>
            <div className="stat-label">Blood Group</div>
          </div>
          <div className="stat-card">
            <div className="stat-value" style={{ color: donor.is_available ? 'var(--success)' : 'var(--danger)' }}>
              {donor.is_available ? 'Available' : 'Unavailable'}
            </div>
            <div className="stat-label">Status</div>
          </div>
          <div className="stat-card">
            <div className="stat-value" style={{ color: can_donate ? 'var(--success)' : 'var(--warning)' }}>
              {can_donate ? 'Eligible' : 'Not Yet'}
            </div>
            <div className="stat-label">Donation Eligibility</div>
          </div>
          <div className="stat-card">
            <div className="stat-value">{days_since_donation !== null ? `${days_since_donation}d` : 'N/A'}</div>
            <div className="stat-label">Since Last Donation</div>
          </div>
        </div>

        <div className="card">
          <h3 className="card-header">Profile Summary</h3>
          <div className="card-grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))' }}>
            <p><FaTint color="var(--primary)" /> <strong>Name:</strong> {donor.full_name}</p>
            <p><FaPhone color="var(--primary)" /> <strong>Phone:</strong> {donor.phone}</p>
            <p><FaMapMarkerAlt color="var(--primary)" /> <strong>City:</strong> {donor.city}, {donor.state}</p>
            <p><FaCalendarAlt color="var(--primary)" /> <strong>Age:</strong> {donor.age} years</p>
          </div>
        </div>

        <div className="card mt-2">
          <h3 className="card-header">Switch Role</h3>
          <p className="text-muted mb-2">Need blood instead? Switch to patient role.</p>
          <Link to="/select-role" className="btn btn-outline btn-sm">Switch to Patient</Link>
        </div>
      </div>
    </div>
  );
}
