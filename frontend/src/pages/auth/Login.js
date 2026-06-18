import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import api from '../../api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [recovering, setRecovering] = useState(false);
  const [showRecover, setShowRecover] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setShowRecover(false);
    try {
      const user = await login(email, password);
      toast.success('Welcome back!');
      if (!user.role) navigate('/select-role');
      else if (user.role === 'admin') navigate('/admin/dashboard');
      else if (user.role === 'donor') navigate(user.has_donor_profile ? '/donor/dashboard' : '/donor/register');
      else if (user.role === 'patient') navigate(user.has_patient_profile ? '/patient/dashboard' : '/patient/register');
      else navigate('/');
    } catch (err) {
      const data = err.response?.data;
      if (data?.recoverable) {
        setShowRecover(true);
        toast.error('Account deleted. You can recover it below.');
      } else {
        toast.error(data?.error || 'Login failed.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRecover = async () => {
    setRecovering(true);
    try {
      await api.post('/api/auth/recover-account', { email, password });
      toast.success('Account recovered! Logging in...');
      setShowRecover(false);
      // Now login normally
      const user = await login(email, password);
      if (!user.role) navigate('/select-role');
      else if (user.role === 'admin') navigate('/admin/dashboard');
      else if (user.role === 'donor') navigate(user.has_donor_profile ? '/donor/dashboard' : '/donor/register');
      else if (user.role === 'patient') navigate(user.has_patient_profile ? '/patient/dashboard' : '/patient/register');
      else navigate('/');
    } catch (err) {
      toast.error(err.response?.data?.error || 'Recovery failed.');
    } finally {
      setRecovering(false);
    }
  };

  return (
    <div className="page">
      <div className="container form-card">
        <h1 className="section-title text-center">Login</h1>
        <div className="card">
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Email Address</label>
              <input type="email" className="form-control" value={email} onChange={e => setEmail(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input type="password" className="form-control" value={password} onChange={e => setPassword(e.target.value)} required />
            </div>
            <div style={{ textAlign: 'right', marginBottom: '1rem' }}>
              <Link to="/forgot-password" style={{ fontSize: '0.9rem', color: 'var(--primary, #e74c3c)' }}>
                Forgot Password?
              </Link>
            </div>
            <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
              {loading ? 'Logging in...' : 'Login'}
            </button>
          </form>
          {showRecover && (
            <div className="text-center mt-2">
              <p style={{ color: '#e74c3c', marginBottom: '0.5rem' }}>
                Your account was deleted. Click below to recover it.
              </p>
              <button
                className="btn btn-secondary"
                onClick={handleRecover}
                disabled={recovering}
                style={{ width: '100%' }}
              >
                {recovering ? 'Recovering...' : 'Recover My Account'}
              </button>
            </div>
          )}
          <p className="text-center mt-2 text-muted">
            Don't have an account? <Link to="/register">Register here</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
