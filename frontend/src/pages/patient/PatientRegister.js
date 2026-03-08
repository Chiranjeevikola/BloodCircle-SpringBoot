import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import API from '../../api';
import { toast } from 'react-toastify';
import { BLOOD_GROUPS } from '../../constants';

export default function PatientRegister() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    full_name: '', phone: '', blood_group_required: 'A+',
    hospital_name: '', city: '', state: '', pincode: '', medical_condition: '',
    urgency_level: 'Normal', required_by_date: '',
  });
  const [loading, setLoading] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await API.post('/patient/register', form);
      toast.success('Patient profile registered!');
      navigate('/patient/dashboard');
    } catch (err) {
      toast.error(err.response?.data?.error || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="container" style={{ maxWidth: '700px' }}>
        <h1 className="section-title text-center">Register as Patient</h1>
        <div className="card">
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group"><label>Full Name *</label><input className="form-control" value={form.full_name} onChange={set('full_name')} required /></div>
              <div className="form-group"><label>Phone *</label><input className="form-control" value={form.phone} onChange={set('phone')} required /></div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Blood Group Required *</label>
                <select className="form-control" value={form.blood_group_required} onChange={set('blood_group_required')} required>
                  {BLOOD_GROUPS.map(bg => <option key={bg} value={bg}>{bg}</option>)}
                </select>
              </div>
              <div className="form-group"><label>Hospital Name *</label><input className="form-control" value={form.hospital_name} onChange={set('hospital_name')} required /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>City *</label><input className="form-control" value={form.city} onChange={set('city')} required /></div>
              <div className="form-group"><label>State *</label><input className="form-control" value={form.state} onChange={set('state')} required /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Pincode *</label><input className="form-control" value={form.pincode} onChange={set('pincode')} required /></div>
              <div className="form-group"><label>Required By Date *</label><input type="date" className="form-control" value={form.required_by_date} onChange={set('required_by_date')} required /></div>
            </div>
            <div className="form-group">
              <label>Urgency Level *</label>
              <select className="form-control" value={form.urgency_level} onChange={set('urgency_level')} required>
                <option value="Normal">Normal</option>
                <option value="Urgent">Urgent</option>
                <option value="Critical">Critical</option>
              </select>
            </div>
            <div className="form-group"><label>Medical Condition</label><textarea className="form-control" value={form.medical_condition} onChange={set('medical_condition')} placeholder="Describe your condition..." /></div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>{loading ? 'Registering...' : 'Register as Patient'}</button>
          </form>
        </div>
      </div>
    </div>
  );
}
