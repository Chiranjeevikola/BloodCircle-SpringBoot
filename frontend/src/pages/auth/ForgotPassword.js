import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api';

export default function ForgotPassword() {
  const [step, setStep] = useState(1); // 1=email, 2=otp, 3=new password
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const navigate = useNavigate();

  const startCountdown = () => {
    setCountdown(60);
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) { clearInterval(timer); return 0; }
        return prev - 1;
      });
    }, 1000);
  };

  const handleSendOtp = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/forgot-password', { email });
      toast.success('OTP sent to your email!');
      setStep(2);
      startCountdown();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to send OTP.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await api.post('/auth/verify-otp', { email, otp });
      if (res.data.verified) {
        toast.success('OTP verified!');
        setStep(3);
      }
    } catch (err) {
      toast.error(err.response?.data?.error || 'Invalid OTP.');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      toast.error('Passwords do not match.');
      return;
    }
    if (newPassword.length < 6) {
      toast.error('Password must be at least 6 characters.');
      return;
    }
    setLoading(true);
    try {
      await api.post('/auth/reset-password', {
        email, otp, new_password: newPassword, confirm_password: confirmPassword,
      });
      toast.success('Password reset successfully!');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.error || 'Reset failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    setLoading(true);
    try {
      await api.post('/auth/forgot-password', { email });
      toast.success('New OTP sent!');
      setOtp('');
      startCountdown();
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to resend OTP.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="container form-card">
        <h1 className="section-title text-center">Reset Password</h1>
        <div className="card">

          {/* Step indicator */}
          <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginBottom: '1.5rem' }}>
            {[1, 2, 3].map(s => (
              <div key={s} style={{
                width: '2.5rem', height: '2.5rem', borderRadius: '50%',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontWeight: 'bold', fontSize: '0.9rem',
                background: step >= s ? 'var(--primary, #e74c3c)' : '#e0e0e0',
                color: step >= s ? '#fff' : '#999',
                transition: 'all 0.3s ease',
              }}>
                {s}
              </div>
            ))}
          </div>

          {/* Step 1: Enter email */}
          {step === 1 && (
            <form onSubmit={handleSendOtp}>
              <p className="text-muted text-center" style={{ marginBottom: '1rem' }}>
                Enter your registered email to receive a 6-digit OTP.
              </p>
              <div className="form-group">
                <label>Email Address</label>
                <input type="email" className="form-control" value={email}
                  onChange={e => setEmail(e.target.value)} required placeholder="you@example.com" />
              </div>
              <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
                {loading ? 'Sending OTP...' : 'Send OTP'}
              </button>
            </form>
          )}

          {/* Step 2: Enter OTP */}
          {step === 2 && (
            <form onSubmit={handleVerifyOtp}>
              <p className="text-muted text-center" style={{ marginBottom: '1rem' }}>
                Enter the 6-digit OTP sent to <strong>{email}</strong>
              </p>
              <div className="form-group">
                <label>OTP Code</label>
                <input type="text" className="form-control" value={otp}
                  onChange={e => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  required maxLength={6} placeholder="123456"
                  style={{ textAlign: 'center', fontSize: '1.5rem', letterSpacing: '0.5rem' }} />
              </div>
              <button type="submit" className="btn btn-primary btn-block" disabled={loading || otp.length !== 6}>
                {loading ? 'Verifying...' : 'Verify OTP'}
              </button>
              <div className="text-center mt-2">
                {countdown > 0 ? (
                  <p className="text-muted">Resend OTP in {countdown}s</p>
                ) : (
                  <button type="button" className="btn btn-link" onClick={handleResendOtp} disabled={loading}
                    style={{ background: 'none', border: 'none', color: 'var(--primary, #e74c3c)', cursor: 'pointer', textDecoration: 'underline' }}>
                    Resend OTP
                  </button>
                )}
              </div>
              <div className="text-center mt-1">
                <button type="button" className="btn btn-link" onClick={() => setStep(1)}
                  style={{ background: 'none', border: 'none', color: '#666', cursor: 'pointer', fontSize: '0.9rem' }}>
                  ← Change email
                </button>
              </div>
            </form>
          )}

          {/* Step 3: New password */}
          {step === 3 && (
            <form onSubmit={handleResetPassword}>
              <p className="text-muted text-center" style={{ marginBottom: '1rem' }}>
                OTP verified! Set your new password.
              </p>
              <div className="form-group">
                <label>New Password</label>
                <input type="password" className="form-control" value={newPassword}
                  onChange={e => setNewPassword(e.target.value)} required minLength={6}
                  placeholder="At least 6 characters" />
              </div>
              <div className="form-group">
                <label>Confirm Password</label>
                <input type="password" className="form-control" value={confirmPassword}
                  onChange={e => setConfirmPassword(e.target.value)} required minLength={6}
                  placeholder="Re-enter password" />
              </div>
              <button type="submit" className="btn btn-primary btn-block" disabled={loading}>
                {loading ? 'Resetting...' : 'Reset Password'}
              </button>
            </form>
          )}

          <p className="text-center mt-2 text-muted">
            Remember your password? <Link to="/login">Login here</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
