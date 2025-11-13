import React, { useEffect, useState } from "react";
import { BrowserRouter, Routes, Route, Link, useNavigate } from "react-router-dom";
import ComplaintForm from "./components/ComplaintForm";
import Dashboard from "./components/Dashboard";
import Login from "./components/Login";
import Signup from "./components/Signup";
import AdminLogin from "./components/AdminLogin";
import AdminDashboard from "./components/AdminDashboard";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminComplaints from "./components/AdminComplaints";
import "./App.css";

function Header({ user, role, onLogout }) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <header className="ios-header">
      <div className="ios-header-container">
        {/* Logo */}
        <Link to="/" className="ios-logo">
          <span className="logo-icon">📱</span>
          <span className="logo-text">E-Mysore</span>
        </Link>

        {/* Mobile Menu Toggle */}
        <button
          className="mobile-menu-toggle"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        >
          <span></span>
          <span></span>
          <span></span>
        </button>

        {/* Navigation */}
        <nav className={`ios-nav ${mobileMenuOpen ? "open" : ""}`}>
          <Link to="/" className="nav-link">
            Dashboard
          </Link>
          <Link to="/complaint" className="nav-link">
            Report
          </Link>

          {user ? (
            <>
              <div className="nav-user-section">
                <span className="nav-username">{user}</span>
                {role === "ADMIN" && (
                  <Link to="/admin" className="nav-link nav-admin">
                    Admin
                  </Link>
                )}
                <button onClick={onLogout} className="nav-logout-btn">
                  Logout
                </button>
              </div>
            </>
          ) : (
            <>
              <Link to="/admin/login" className="nav-link">
                Admin Login
              </Link>
              <Link to="/login" className="nav-link">
                Login
              </Link>
              <Link to="/register" className="nav-link nav-signup">
                Sign up
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}

function AppInner() {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const t = localStorage.getItem("emysore_token");
    const u = localStorage.getItem("emysore_user");
    const r = localStorage.getItem("emysore_role");
    if (t && u) {
      setUser(u);
      if (r) setRole(r);
    }
  }, []);

  const handleLogin = (username, tok) => {
    setUser(username);
    const r = localStorage.getItem("emysore_role");
    if (r) setRole(r);
  };

  const handleLogout = () => {
    localStorage.removeItem("emysore_token");
    localStorage.removeItem("emysore_user");
    localStorage.removeItem("emysore_role");
    setUser(null);
    setRole(null);
    navigate("/");
  };

  return (
    <div className="App">
      <Header user={user} role={role} onLogout={handleLogout} />

      <main>
        <Routes>
          <Route path="/" element={<Dashboard user={user} role={role} />} />
          <Route path="/login" element={<Login onLogin={handleLogin} />} />
          <Route path="/register" element={<Signup onSignup={() => {}} />} />

          <Route path="/complaint" element={
            <ProtectedRoute requireRole={"CITIZEN"}>
              <div style={{maxWidth:980, margin:'24px auto', padding:'0 16px'}}>
                <ComplaintForm />
              </div>
            </ProtectedRoute>
          } />

          <Route path="/admin/login" element={<AdminLogin />} />
          <Route path="/admin" element={
            <ProtectedRoute requireRole={"ADMIN"}>
              <AdminDashboard />
            </ProtectedRoute>
          } />
          <Route path="/admin/complaints" element={
            <ProtectedRoute requireRole={"ADMIN"}>
              <AdminComplaints />
            </ProtectedRoute>
          } />

          {/* fallback */}
          <Route path="*" element={<Dashboard user={user} role={role} />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppInner />
    </BrowserRouter>
  );
}
