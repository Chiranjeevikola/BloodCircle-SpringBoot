import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import API from '../../api';
import { FaTint, FaClipboardList, FaSearch, FaExclamationTriangle } from 'react-icons/fa';

export default function PatientDashboard() {
  const [data, setData] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    API.get('/patient/dashboard')
      .then(r => setData(r.data))
      .catch(() => navigate('/patient/register'));
  }, [navigate]);

  if (!data) return <div className="loading">Loading...</div>;

  const { patient, matching_donors } = data;

  const urgencyClass = patient.urgency_level === 'Critical' ? 'badge-danger'
    : patient.urgency_level === 'Urgent' ? 'badge-warning' : 'badge-primary';

  return (
    <div className="page">
      <div className="container">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', marginBottom: '2rem' }}>
          <h1 className="section-title" style={{ margin: 0 }}>Patient Dashboard</h1>
          <Link to="/patient/search" className="btn btn-primary"><FaSearch /> Search Donors</Link>
        </div>

        <div className="stats-grid">
          <div className="stat-card"><FaTint className="stat-icon" /><div className="stat-value">{patient.blood_group}</div><div className="stat-label">Blood Group</div></div>
          <div className="stat-card"><FaClipboardList className="stat-icon" /><div className="stat-value">{matching_donors ? matching_donors.length : 0}</div><div className="stat-label">Compatible Donors</div></div>
          <div className="stat-card"><FaExclamationTriangle className="stat-icon" /><div className="stat-value"><span className={`badge ${urgencyClass}`}>{patient.urgency_level}</span></div><div className="stat-label">Urgency</div></div>
        </div>

        {patient.is_fulfilled && (
          <div className="alert alert-success">Your blood request has been fulfilled. If you need blood again, please contact admin to reopen your request.</div>
        )}

        <div className="card" style={{ marginTop: '2rem' }}>
          <h2>Recent Compatible Donors</h2>
          {matching_donors && matching_donors.length > 0 ? (
            <div className="table-responsive">
              <table className="table">
                <thead><tr><th>Name</th><th>Blood Group</th><th>City</th><th>Phone</th><th>Action</th></tr></thead>
                <tbody>
                  {matching_donors.map(d => (
                    <tr key={d.id}>
                      <td>{d.full_name}</td>
                      <td><span className="badge badge-danger">{d.blood_group}</span></td>
                      <td>{d.city}</td>
                      <td>{d.phone}</td>
                      <td><Link to={`/patient/donor/${d.id}`} className="btn btn-sm btn-outline">View</Link></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : <p>No compatible donors found nearby. Try <Link to="/patient/search">searching</Link> with different filters.</p>}
        </div>

        <div style={{ marginTop: '1.5rem', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <Link to="/patient/profile" className="btn btn-outline">View Profile</Link>
          <Link to="/patient/edit-profile" className="btn btn-secondary">Edit Profile</Link>
        </div>

        <div className="card mt-2">
          <h3 className="card-header">Switch Role</h3>
          <p className="text-muted mb-2">Want to donate blood? Switch to donor role.</p>
          <Link to="/select-role?switch=true" className="btn btn-outline btn-sm">Switch to Donor</Link>
        </div>
      </div>
    </div>
  );
}
