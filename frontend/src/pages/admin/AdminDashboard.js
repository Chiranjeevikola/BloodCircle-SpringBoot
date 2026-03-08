import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import API from '../../api';
import { FaUsers, FaTint, FaHospital, FaComments, FaUserShield } from 'react-icons/fa';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    API.get('/admin/dashboard').then(r => setStats(r.data)).catch(() => {});
  }, []);

  if (!stats) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title"><FaUserShield /> Admin Dashboard</h1>

        <div className="stats-grid">
          <div className="stat-card"><FaUsers className="stat-icon" /><div className="stat-number">{stats.total_users}</div><div className="stat-label">Total Users</div></div>
          <div className="stat-card"><FaTint className="stat-icon" /><div className="stat-number">{stats.total_donors}</div><div className="stat-label">Donors</div></div>
          <div className="stat-card"><FaHospital className="stat-icon" /><div className="stat-number">{stats.total_patients}</div><div className="stat-label">Patients</div></div>
          <div className="stat-card"><FaComments className="stat-icon" /><div className="stat-number">{stats.total_feedback}</div><div className="stat-label">Feedback</div></div>
        </div>

        <div className="stats-grid" style={{ marginTop: '1rem' }}>
          <div className="stat-card"><div className="stat-number">{stats.active_donors}</div><div className="stat-label">Active Donors</div></div>
          <div className="stat-card"><div className="stat-number">{stats.active_patients}</div><div className="stat-label">Active Patients</div></div>
          <div className="stat-card"><div className="stat-number">{stats.blocked_users}</div><div className="stat-label">Blocked Users</div></div>
          <div className="stat-card"><div className="stat-number">{stats.unread_feedback}</div><div className="stat-label">Unread Feedback</div></div>
        </div>

        <div className="card" style={{ marginTop: '2rem' }}>
          <h2>Quick Actions</h2>
          <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
            <Link to="/admin/users" className="btn btn-primary"><FaUsers /> Manage Users</Link>
            <Link to="/admin/donors" className="btn btn-primary"><FaTint /> Manage Donors</Link>
            <Link to="/admin/patients" className="btn btn-primary"><FaHospital /> Manage Patients</Link>
            <Link to="/admin/feedback" className="btn btn-primary"><FaComments /> Manage Feedback</Link>
          </div>
        </div>

        {stats.blood_group_stats && (
          <div className="card" style={{ marginTop: '2rem' }}>
            <h2>Donors by Blood Group</h2>
            <div className="stats-grid">
              {Object.entries(stats.blood_group_stats).map(([bg, count]) => (
                <div key={bg} className="stat-card">
                  <div className="stat-number" style={{ color: 'var(--primary)' }}>{bg}</div>
                  <div className="stat-label">{count} donors</div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
