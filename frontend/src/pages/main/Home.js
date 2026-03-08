import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { FaHandHoldingHeart, FaTint, FaUsers, FaSearch } from 'react-icons/fa';
import API from '../../api';

export default function Home() {
  const [stats, setStats] = useState({ total_donors: 0, available_donors: 0, total_patients: 0 });

  useEffect(() => {
    API.get('/stats').then(r => setStats(r.data)).catch(() => {});
  }, []);

  return (
    <>
      <section className="hero">
        <div className="container">
          <h1>Save Lives Through Blood Donation</h1>
          <p>Connect blood donors with patients in need. Every drop counts — join our community and make a difference today.</p>
          <div className="flex gap-1" style={{ justifyContent: 'center' }}>
            <Link to="/register" className="btn btn-lg" style={{ background: '#fff', color: 'var(--primary)' }}>Become a Donor</Link>
            <Link to="/register" className="btn btn-lg btn-outline" style={{ borderColor: '#fff', color: '#fff' }}>Need Blood?</Link>
          </div>
        </div>
      </section>

      <section className="page">
        <div className="container">
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-value">{stats.total_donors}</div>
              <div className="stat-label">Registered Donors</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{stats.available_donors}</div>
              <div className="stat-label">Available Donors</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">{stats.total_patients}</div>
              <div className="stat-label">Patients Helped</div>
            </div>
            <div className="stat-card">
              <div className="stat-value">8</div>
              <div className="stat-label">Blood Groups Supported</div>
            </div>
          </div>

          <h2 className="section-title text-center">How BloodCircle Works</h2>
          <div className="card-grid" style={{ marginBottom: '3rem' }}>
            <div className="card text-center">
              <FaUsers style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '1rem' }} />
              <h3>Register</h3>
              <p className="text-muted mt-1">Create your account as a donor or patient in minutes.</p>
            </div>
            <div className="card text-center">
              <FaSearch style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '1rem' }} />
              <h3>Search & Connect</h3>
              <p className="text-muted mt-1">Find compatible donors by blood group, city, and availability.</p>
            </div>
            <div className="card text-center">
              <FaTint style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '1rem' }} />
              <h3>Donate Blood</h3>
              <p className="text-muted mt-1">Connect directly with patients and save lives.</p>
            </div>
            <div className="card text-center">
              <FaHandHoldingHeart style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '1rem' }} />
              <h3>Save Lives</h3>
              <p className="text-muted mt-1">Every donation can save up to three lives.</p>
            </div>
          </div>

          <div className="text-center">
            <Link to="/register" className="btn btn-primary btn-lg">Join BloodCircle Today</Link>
          </div>
        </div>
      </section>
    </>
  );
}
