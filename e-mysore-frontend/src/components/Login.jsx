import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./Login.css";

function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("http://localhost:8080/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Login failed");
      localStorage.setItem("emysore_token", data.token);
      localStorage.setItem("emysore_user", data.username);
      if (data.role) localStorage.setItem("emysore_role", data.role);
      onLogin && onLogin(data.username, data.token);
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ios-container">
      <div className="ios-login-wrapper">
        {/* Header */}
        <div className="ios-header">
          <h1 className="ios-title">Welcome back</h1>
          <p className="ios-subtitle">Sign in to your e-mysore account</p>
        </div>

        {/* Form Card */}
        <form className="ios-form" onSubmit={submit}>
          {/* Username Input */}
          <div className="ios-input-group">
            <input
              type="text"
              className="ios-input"
              placeholder="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          {/* Password Input */}
          <div className="ios-input-group">
            <input
              type="password"
              className="ios-input"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {/* Error Message */}
          {error && (
            <div className="ios-error-message">
              <span>⚠️</span> {error}
            </div>
          )}

          {/* Sign In Button */}
          <button 
            type="submit" 
            disabled={loading}
            className="ios-button ios-button-primary"
          >
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>

        {/* Footer */}
        <div className="ios-footer">
          <p>
            Don't have an account?{" "}
            <Link to="/signup" className="ios-link">
              Create one
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Login;
