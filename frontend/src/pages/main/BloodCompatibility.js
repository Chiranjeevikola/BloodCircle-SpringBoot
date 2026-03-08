import React, { useEffect, useState } from 'react';
import API from '../../api';

const groups = ['O-', 'O+', 'A-', 'A+', 'B-', 'B+', 'AB-', 'AB+'];

export default function BloodCompatibility() {
  const [compat, setCompat] = useState(null);

  useEffect(() => {
    API.get('/blood-compatibility').then(r => setCompat(r.data.compatibility)).catch(() => {});
  }, []);

  if (!compat) return <div className="loading">Loading...</div>;

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title text-center">Blood Compatibility Guide</h1>
        <p className="text-center text-muted mb-3">
          This chart shows which blood types can donate to and receive from other types.
        </p>

        <div className="card table-responsive mb-3">
          <table className="blood-compat-table">
            <thead>
              <tr>
                <th>Donor →<br/>Recipient ↓</th>
                {groups.map(g => <th key={g}><span className="badge badge-blood">{g}</span></th>)}
              </tr>
            </thead>
            <tbody>
              {groups.map(recipient => (
                <tr key={recipient}>
                  <td><strong>{recipient}</strong></td>
                  {groups.map(donor => {
                    const canDonate = compat[donor]?.can_donate_to?.includes(recipient);
                    return (
                      <td key={donor} className={canDonate ? 'compat-yes' : 'compat-no'}>
                        {canDonate ? '✓' : '—'}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="card-grid">
          {groups.map(bg => (
            <div key={bg} className="card">
              <h3><span className="badge badge-blood">{bg}</span></h3>
              <p className="mt-1"><strong>Can donate to:</strong> {compat[bg]?.can_donate_to?.join(', ')}</p>
              <p className="mt-1"><strong>Can receive from:</strong> {compat[bg]?.can_receive_from?.join(', ')}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
