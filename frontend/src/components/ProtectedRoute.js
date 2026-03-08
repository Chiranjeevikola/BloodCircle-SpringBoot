import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function ProtectedRoute({ children, roles }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="loading">Loading...</div>;
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />;
  return children;
}

export function GuestRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="loading">Loading...</div>;
  if (user) {
    if (!user.role) return <Navigate to="/select-role" replace />;
    if (user.role === 'admin') return <Navigate to="/admin/dashboard" replace />;
    if (user.role === 'donor') return <Navigate to={user.has_donor_profile ? '/donor/dashboard' : '/donor/register'} replace />;
    if (user.role === 'patient') return <Navigate to={user.has_patient_profile ? '/patient/dashboard' : '/patient/register'} replace />;
    return <Navigate to="/" replace />;
  }
  return children;
}
