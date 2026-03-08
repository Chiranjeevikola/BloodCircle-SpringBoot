import React from 'react';
import { FaHeartbeat } from 'react-icons/fa';

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container">
        <p><FaHeartbeat /> BloodCircle &copy; {new Date().getFullYear()} — Save Lives Through Blood Donation</p>
        <p style={{ fontSize: '0.8rem', marginTop: '0.5rem', opacity: 0.7 }}>
          <a href="/privacy-policy">Privacy Policy</a> &middot; <a href="/faq">FAQ</a> &middot; <a href="/feedback">Contact Us</a>
        </p>
      </div>
    </footer>
  );
}
