import React, { useState } from 'react';
import API from '../../api';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';

export default function FeedbackPage() {
  const { user } = useAuth();
  const [form, setForm] = useState({
    name: '', email: user?.email || '', subject: '', message: '', rating: '',
  });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await API.post('/feedback', form);
      toast.success('Thank you for your feedback!');
      setForm({ name: '', email: user?.email || '', subject: '', message: '', rating: '' });
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to submit feedback.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="container form-card">
        <h1 className="section-title text-center">Send Us Feedback</h1>
        <div className="card">
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Your Name</label>
              <input className="form-control" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input type="email" className="form-control" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Subject</label>
              <input className="form-control" value={form.subject} onChange={e => setForm({ ...form, subject: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Message</label>
              <textarea className="form-control" value={form.message} onChange={e => setForm({ ...form, message: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Rating (Optional)</label>
              <select className="form-control" value={form.rating} onChange={e => setForm({ ...form, rating: e.target.value })}>
                <option value="">Select Rating</option>
                <option value="5">⭐⭐⭐⭐⭐ Excellent</option>
                <option value="4">⭐⭐⭐⭐ Very Good</option>
                <option value="3">⭐⭐⭐ Good</option>
                <option value="2">⭐⭐ Fair</option>
                <option value="1">⭐ Poor</option>
              </select>
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Sending...' : 'Submit Feedback'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
