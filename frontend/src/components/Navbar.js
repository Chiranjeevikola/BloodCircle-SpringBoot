import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FaHeartbeat } from 'react-icons/fa';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <div className="container">
        <NavLink to="/" className="navbar-brand">
          <FaHeartbeat /> Blood<span>Circle</span>
        </NavLink>
        <div className="nav-links">
          <NavLink to="/">Home</NavLink>
          <NavLink to="/about">About</NavLink>
          <NavLink to="/how-it-works">How It Works</NavLink>
          <NavLink to="/blood-compatibility">Compatibility</NavLink>
          <NavLink to="/faq">FAQ</NavLink>
          <NavLink to="/feedback">Feedback</NavLink>

          {!user ? (
            <>
              <NavLink to="/login" className="btn btn-outline btn-sm">Login</NavLink>
              <NavLink to="/register" className="btn btn-primary btn-sm">Register</NavLink>
            </>
          ) : (
            <>
              {user.role === 'donor' && <NavLink to="/donor/dashboard">Dashboard</NavLink>}
              {user.role === 'patient' && <NavLink to="/patient/dashboard">Dashboard</NavLink>}
              {user.role === 'admin' && <NavLink to="/admin/dashboard">Admin</NavLink>}
              {!user.role && <NavLink to="/select-role">Select Role</NavLink>}
              <button onClick={handleLogout} className="btn btn-outline btn-sm">Logout</button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
