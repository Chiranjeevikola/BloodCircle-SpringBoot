import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { toast } from 'react-toastify';
import { FaTint, FaHospitalUser } from 'react-icons/fa';
import API from '../../api';

export default function SelectRole() {
  const { user, refreshUser } = useAuth();
  const navigate = useNavigate();

  const selectRole = async (role) => {
    try {
      await API.post('/auth/select-role', { role });
      await refreshUser();
      toast.success(`Role set to ${role}. Please complete your profile.`);
      navigate(`/${role}/register`);
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to set role.');
    }
  };

  if (user?.role) {
    // Already has role, redirect
    if (user.role === 'donor') navigate('/donor/dashboard');
    if (user.role === 'patient') navigate('/patient/dashboard');
    if (user.role === 'admin') navigate('/admin/dashboard');
  }

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title text-center">Choose Your Role</h1>
        <p className="text-center text-muted mb-3">How would you like to use BloodCircle?</p>
        <div className="role-cards">
          <div className="role-card" onClick={() => selectRole('donor')}>
            <div className="icon"><FaTint color="var(--primary)" /></div>
            <h3>Blood Donor</h3>
            <p>I want to donate blood and help save lives</p>
          </div>
          <div className="role-card" onClick={() => selectRole('patient')}>
            <div className="icon"><FaHospitalUser color="var(--primary)" /></div>
            <h3>Patient</h3>
            <p>I need blood and want to find donors</p>
          </div>
        </div>
      </div>
    </div>
  );
}
