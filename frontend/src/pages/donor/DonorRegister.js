import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import API from '../../api';
import { toast } from 'react-toastify';
import { BLOOD_GROUPS, GENDERS } from '../../constants';

export default function DonorRegister() {
  const { refreshUser } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    full_name: '', phone: '', blood_group: '', date_of_birth: '',
    gender: '', city: '', state: '', pincode: '',
    last_donation_date: '', medical_history: '', is_available: true,
  });

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await API.post('/donor/register', form);
      await refreshUser();
      toast.success('Donor profile created!');
      navigate('/donor/dashboard');
    } catch (err) {
      toast.error(err.response?.data?.error || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: '700px' }}>
        <h1 className="section-title text-center">Donor Registration</h1>
        <div className="card">
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Full Name *</label>
                <input className="form-control" value={form.full_name} onChange={set('full_name')} required />
              </div>
              <div className="form-group">
                <label>Phone Number *</label>
                <input className="form-control" value={form.phone} onChange={set('phone')} required />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Blood Group *</label>
                <select className="form-control" value={form.blood_group} onChange={set('blood_group')} required>
                  <option value="">Select Blood Group</option>
                  {BLOOD_GROUPS.map(bg => <option key={bg} value={bg}>{bg}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Gender *</label>
                <select className="form-control" value={form.gender} onChange={set('gender')} required>
                  <option value="">Select Gender</option>
                  {GENDERS.map(g => <option key={g} value={g}>{g}</option>)}
                </select>
              </div>
            </div>
            <div className="form-group">
              <label>Date of Birth *</label>
              <input type="date" className="form-control" value={form.date_of_birth} onChange={set('date_of_birth')} required />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>City *</label>
                <input className="form-control" value={form.city} onChange={set('city')} required />
              </div>
              <div className="form-group">
                <label>State *</label>
                <input className="form-control" value={form.state} onChange={set('state')} required />
              </div>
            </div>
            <div className="form-group">
              <label>Pincode *</label>
              <input className="form-control" value={form.pincode} onChange={set('pincode')} required />
            </div>
            <div className="form-group">
              <label>Last Donation Date (Optional)</label>
              <input type="date" className="form-control" value={form.last_donation_date} onChange={set('last_donation_date')} />
            </div>
            <div className="form-group">
              <label>Medical History (Optional)</label>
              <textarea className="form-control" value={form.medical_history} onChange={set('medical_history')} />
            </div>
            <div className="form-group">
              <label>
                <input type="checkbox" checked={form.is_available} onChange={set('is_available')} />{' '}
                I am available to donate
              </label>
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Saving...' : 'Complete Registration'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
