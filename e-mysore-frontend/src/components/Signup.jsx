import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import "./Signup.css";

function Signup({ onSignup }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.error || "Signup failed");
      setSuccess("Account created. You can now log in.");
      onSignup && onSignup(data.username);
      // redirect to login so user can sign in
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ios-container">
      <div className="ios-signup-wrapper">
        {/* Header */}
        <div className="ios-header">
          <h1 className="ios-title">Get started</h1>
          <p className="ios-subtitle">Create your e-mysore account</p>
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

          {/* Success Message */}
          {success && (
            <div className="ios-success-message">
              <span>✓</span> {success}
            </div>
          )}

          {/* Create Account Button */}
          <button 
            type="submit" 
            disabled={loading || success}
            className="ios-button ios-button-primary"
          >
            {loading ? "Creating..." : success ? "Redirecting..." : "Create account"}
          </button>
        </form>

        {/* Footer */}
        <div className="ios-footer">
          <p>
            Already have an account?{" "}
            <Link to="/login" className="ios-link">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

export default Signup;
