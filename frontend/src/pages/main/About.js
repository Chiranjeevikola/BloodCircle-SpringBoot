import React from 'react';
import { FaHeartbeat, FaShieldAlt, FaUsers } from 'react-icons/fa';

export default function About() {
  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title">About BloodCircle</h1>
        <div className="card mb-3">
          <p style={{ fontSize: '1.1rem', lineHeight: '1.8' }}>
            BloodCircle is a platform dedicated to bridging the gap between blood donors and patients in need.
            Our mission is to make blood donation accessible, efficient, and life-saving. We connect donors with 
            patients based on blood group compatibility, location, and availability — ensuring help reaches those 
            who need it most.
          </p>
        </div>

        <div className="card-grid">
          <div className="card text-center">
            <FaHeartbeat style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '1rem' }} />
            <h3>Our Mission</h3>
            <p className="text-muted mt-1">To save lives by connecting blood donors with patients quickly and efficiently.</p>
          </div>
          <div className="card text-center">
            <FaShieldAlt style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '1rem' }} />
            <h3>Safe & Secure</h3>
            <p className="text-muted mt-1">We use industry-standard security to protect your data and privacy.</p>
          </div>
          <div className="card text-center">
            <FaUsers style={{ fontSize: '2.5rem', color: 'var(--primary)', marginBottom: '1rem' }} />
            <h3>Community Driven</h3>
            <p className="text-muted mt-1">Built by the community, for the community. Every donation matters.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
