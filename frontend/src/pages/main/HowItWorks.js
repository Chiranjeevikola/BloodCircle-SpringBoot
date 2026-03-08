import React from 'react';

export default function HowItWorks() {
  const steps = [
    { num: '1', title: 'Create an Account', desc: 'Register with your email and password. It takes less than a minute.' },
    { num: '2', title: 'Choose Your Role', desc: 'Select whether you want to be a blood donor or a patient seeking blood.' },
    { num: '3', title: 'Complete Your Profile', desc: 'Fill in your details including blood group, location, and contact info.' },
    { num: '4', title: 'Search & Connect', desc: 'Patients can search for compatible donors by blood group and city.' },
    { num: '5', title: 'Contact & Donate', desc: 'View donor details and reach out directly to arrange the donation.' },
    { num: '6', title: 'Save Lives', desc: 'Your donation can save up to three lives. Thank you for being a hero!' },
  ];

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title text-center">How BloodCircle Works</h1>
        <div className="card-grid">
          {steps.map((s) => (
            <div key={s.num} className="card">
              <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--primary)', marginBottom: '0.5rem' }}>
                Step {s.num}
              </div>
              <h3>{s.title}</h3>
              <p className="text-muted mt-1">{s.desc}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
