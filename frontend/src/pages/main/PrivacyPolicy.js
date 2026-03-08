import React from 'react';

export default function PrivacyPolicy() {
  return (
    <div className="page">
      <div className="container" style={{ maxWidth: '800px' }}>
        <h1 className="section-title">Privacy Policy</h1>
        <div className="card">
          <h3>Data Collection</h3>
          <p className="text-muted mt-1 mb-3">We collect your name, email, phone number, blood group, location, and medical information to connect donors with patients.</p>
          <h3>Data Usage</h3>
          <p className="text-muted mt-1 mb-3">Your data is used solely for matching blood donors with patients. We do not sell or share your data with third parties.</p>
          <h3>Data Security</h3>
          <p className="text-muted mt-1 mb-3">We use password hashing, JWT authentication, and encrypted connections to protect your information.</p>
          <h3>Your Rights</h3>
          <p className="text-muted mt-1 mb-3">You can delete your account at any time. Deleted accounts can be recovered within 30 days.</p>
          <h3>Contact</h3>
          <p className="text-muted mt-1">If you have questions about your data, please use our feedback form to contact us.</p>
        </div>
      </div>
    </div>
  );
}
