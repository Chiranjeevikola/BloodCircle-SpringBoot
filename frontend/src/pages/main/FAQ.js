import React, { useState } from 'react';

const faqs = [
  { q: 'Who can donate blood?', a: 'Anyone between 18-65 years of age, weighing at least 50 kg, and in good health can donate blood.' },
  { q: 'How often can I donate blood?', a: 'You can donate blood every 3 months (90 days). Our system tracks your last donation date automatically.' },
  { q: 'Is blood donation safe?', a: 'Yes, blood donation is completely safe. Sterile, single-use equipment is used for each donation.' },
  { q: 'How long does the donation process take?', a: 'The actual donation takes about 10-15 minutes, but the entire process including registration takes about 45 minutes.' },
  { q: 'What blood types are compatible?', a: 'O- is the universal donor. AB+ is the universal recipient. Check our Blood Compatibility page for full details.' },
  { q: 'How do I search for blood donors?', a: 'After registering as a patient, you can search for donors by blood type, location, and availability.' },
  { q: 'How is my data protected?', a: 'We use industry-standard security including password hashing, JWT authentication, and secure connections.' },
  { q: 'Can I update my availability status?', a: 'Yes! Donors can toggle their availability status anytime from their dashboard.' },
];

export default function FAQ() {
  const [open, setOpen] = useState(null);

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: '800px' }}>
        <h1 className="section-title text-center">Frequently Asked Questions</h1>
        {faqs.map((f, i) => (
          <div
            key={i}
            className="card mb-2"
            style={{ cursor: 'pointer' }}
            onClick={() => setOpen(open === i ? null : i)}
          >
            <div className="flex justify-between items-center">
              <strong>{f.q}</strong>
              <span style={{ fontSize: '1.2rem' }}>{open === i ? '−' : '+'}</span>
            </div>
            {open === i && <p className="text-muted mt-1">{f.a}</p>}
          </div>
        ))}
      </div>
    </div>
  );
}
