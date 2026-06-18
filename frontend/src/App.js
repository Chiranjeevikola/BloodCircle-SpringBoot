import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import { ProtectedRoute, GuestRoute } from './components/ProtectedRoute';

// Public pages
import Home from './pages/main/Home';
import About from './pages/main/About';
import HowItWorks from './pages/main/HowItWorks';
import BloodCompatibility from './pages/main/BloodCompatibility';
import FAQ from './pages/main/FAQ';
import FeedbackPage from './pages/main/FeedbackPage';
import PrivacyPolicy from './pages/main/PrivacyPolicy';

// Auth pages
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import ForgotPassword from './pages/auth/ForgotPassword';
import SelectRole from './pages/auth/SelectRole';

// Donor pages
import DonorRegister from './pages/donor/DonorRegister';
import DonorDashboard from './pages/donor/DonorDashboard';
import DonorProfile from './pages/donor/DonorProfile';
import DonorEditProfile from './pages/donor/DonorEditProfile';

// Patient pages
import PatientRegister from './pages/patient/PatientRegister';
import PatientDashboard from './pages/patient/PatientDashboard';
import PatientProfile from './pages/patient/PatientProfile';
import PatientEditProfile from './pages/patient/PatientEditProfile';
import SearchDonors from './pages/patient/SearchDonors';
import ViewDonor from './pages/patient/ViewDonor';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import ManageUsers from './pages/admin/ManageUsers';
import ManageDonors from './pages/admin/ManageDonors';
import ManagePatients from './pages/admin/ManagePatients';
import ManageFeedback from './pages/admin/ManageFeedback';

export default function App() {
  return (
    <>
      <Navbar />
      <main>
        <Routes>
          {/* Public */}
          <Route path="/" element={<Home />} />
          <Route path="/about" element={<About />} />
          <Route path="/how-it-works" element={<HowItWorks />} />
          <Route path="/blood-compatibility" element={<BloodCompatibility />} />
          <Route path="/faq" element={<FAQ />} />
          <Route path="/feedback" element={<FeedbackPage />} />
          <Route path="/privacy-policy" element={<PrivacyPolicy />} />

          {/* Auth */}
          <Route path="/login" element={<GuestRoute><Login /></GuestRoute>} />
          <Route path="/register" element={<GuestRoute><Register /></GuestRoute>} />
          <Route path="/forgot-password" element={<GuestRoute><ForgotPassword /></GuestRoute>} />
          <Route path="/select-role" element={<ProtectedRoute><SelectRole /></ProtectedRoute>} />

          {/* Donor */}
          <Route path="/donor/register" element={<ProtectedRoute><DonorRegister /></ProtectedRoute>} />
          <Route path="/donor/dashboard" element={<ProtectedRoute roles={['donor']}><DonorDashboard /></ProtectedRoute>} />
          <Route path="/donor/profile" element={<ProtectedRoute roles={['donor']}><DonorProfile /></ProtectedRoute>} />
          <Route path="/donor/edit-profile" element={<ProtectedRoute roles={['donor']}><DonorEditProfile /></ProtectedRoute>} />

          {/* Patient */}
          <Route path="/patient/register" element={<ProtectedRoute><PatientRegister /></ProtectedRoute>} />
          <Route path="/patient/dashboard" element={<ProtectedRoute roles={['patient']}><PatientDashboard /></ProtectedRoute>} />
          <Route path="/patient/profile" element={<ProtectedRoute roles={['patient']}><PatientProfile /></ProtectedRoute>} />
          <Route path="/patient/edit-profile" element={<ProtectedRoute roles={['patient']}><PatientEditProfile /></ProtectedRoute>} />
          <Route path="/patient/search" element={<ProtectedRoute roles={['patient']}><SearchDonors /></ProtectedRoute>} />
          <Route path="/patient/donor/:id" element={<ProtectedRoute roles={['patient']}><ViewDonor /></ProtectedRoute>} />

          {/* Admin */}
          <Route path="/admin/dashboard" element={<ProtectedRoute roles={['admin']}><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/users" element={<ProtectedRoute roles={['admin']}><ManageUsers /></ProtectedRoute>} />
          <Route path="/admin/donors" element={<ProtectedRoute roles={['admin']}><ManageDonors /></ProtectedRoute>} />
          <Route path="/admin/patients" element={<ProtectedRoute roles={['admin']}><ManagePatients /></ProtectedRoute>} />
          <Route path="/admin/feedback" element={<ProtectedRoute roles={['admin']}><ManageFeedback /></ProtectedRoute>} />

          {/* 404 */}
          <Route path="*" element={<div className="page container text-center"><h1>404</h1><p>Page not found</p></div>} />
        </Routes>
      </main>
      <Footer />
    </>
  );
}
