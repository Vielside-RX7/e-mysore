import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./AdminDashboard.css";

export default function AdminDashboard() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchComplaints();
  }, []);

  const fetchComplaints = async () => {
    try {
      setLoading(true);
      setError(null);
      const headers = { 'Accept': 'application/json' };
      const token = localStorage.getItem('emysore_token');
      if (token) headers['Authorization'] = `Bearer ${token}`;
      const res = await fetch("http://localhost:8080/api/complaints?size=100", { headers });
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      // Handle both paginated and non-paginated responses
      const complaintList = data.content || data;
      setComplaints(Array.isArray(complaintList) ? complaintList : []);
      setError(null);
    } catch (e) {
      console.error("Failed to fetch complaints:", e);
      setError(e.message);
      setComplaints([]);
    } finally {
      setLoading(false);
    }
  };

  // Analytics calculations
  const totalComplaints = complaints.length;
  const statusStats = {
    OPEN: complaints.filter(c => c.status === "OPEN").length,
    IN_PROGRESS: complaints.filter(c => c.status === "IN_PROGRESS").length,
    RESOLVED: complaints.filter(c => c.status === "RESOLVED").length,
  };

  const categoryStats = complaints.reduce((acc, complaint) => {
    const cat = complaint.category || "Other";
    acc[cat] = (acc[cat] || 0) + 1;
    return acc;
  }, {});

  const escalatedCount = complaints.filter(c => c.escalated).length;
  const resolutionRate = totalComplaints > 0 
    ? Math.round((statusStats.RESOLVED / totalComplaints) * 100) 
    : 0;

  const categories = Object.keys(categoryStats).sort((a, b) => categoryStats[b] - categoryStats[a]);
  const displayedComplaints = selectedCategory 
    ? complaints.filter(c => (c.category || "Other") === selectedCategory)
    : complaints.slice(0, 10);

  const handleLogout = () => {
    localStorage.removeItem("emysore_token");
    localStorage.removeItem("emysore_user");
    localStorage.removeItem("emysore_role");
    navigate("/admin/login");
  };

  return (
    <div className="admin-dashboard">
      {/* Header */}
      <div className="admin-header">
        <div className="admin-header-content">
          <h1>Admin Dashboard</h1>
          <p>City Complaint Management & Analytics</p>
        </div>
        <button onClick={handleLogout} className="admin-logout-btn">
          Logout
        </button>
      </div>

      {/* Key Metrics */}
      <section className="admin-metrics">
        <div className="metric-card metric-total">
          <div className="metric-icon">📊</div>
          <div className="metric-info">
            <div className="metric-label">Total Complaints</div>
            <div className="metric-value">{totalComplaints}</div>
          </div>
        </div>

        <div className="metric-card metric-open">
          <div className="metric-icon">🔔</div>
          <div className="metric-info">
            <div className="metric-label">Open</div>
            <div className="metric-value">{statusStats.OPEN}</div>
          </div>
        </div>

        <div className="metric-card metric-progress">
          <div className="metric-icon">⏳</div>
          <div className="metric-info">
            <div className="metric-label">In Progress</div>
            <div className="metric-value">{statusStats.IN_PROGRESS}</div>
          </div>
        </div>

        <div className="metric-card metric-resolved">
          <div className="metric-icon">✓</div>
          <div className="metric-info">
            <div className="metric-label">Resolved</div>
            <div className="metric-value">{statusStats.RESOLVED}</div>
          </div>
        </div>

        <div className="metric-card metric-escalated">
          <div className="metric-icon">⚡</div>
          <div className="metric-info">
            <div className="metric-label">Escalated</div>
            <div className="metric-value">{escalatedCount}</div>
          </div>
        </div>

        <div className="metric-card metric-resolution">
          <div className="metric-icon">📈</div>
          <div className="metric-info">
            <div className="metric-label">Resolution Rate</div>
            <div className="metric-value">{resolutionRate}%</div>
          </div>
        </div>
      </section>

      {/* Category Breakdown */}
      <section className="admin-categories">
        <h2>Complaints by Category</h2>
        {loading ? (
          <div className="loading">Loading categories...</div>
        ) : categories.length > 0 ? (
          <div className="category-grid">
            {categories.map(category => (
              <button
                key={category}
                className={`category-card ${selectedCategory === category ? "active" : ""}`}
                onClick={() => setSelectedCategory(selectedCategory === category ? null : category)}
              >
                <div className="category-name">{category}</div>
                <div className="category-count">{categoryStats[category]}</div>
                <div className="category-bar">
                  <div 
                    className="category-bar-fill"
                    style={{
                      width: `${(categoryStats[category] / totalComplaints) * 100}%`
                    }}
                  ></div>
                </div>
              </button>
            ))}
          </div>
        ) : (
          <p className="empty-state">No complaints yet</p>
        )}
      </section>

      {/* Complaints List */}
      <section className="admin-complaints-list">
        <h2>
          {selectedCategory ? `${selectedCategory} Complaints` : "Recent Complaints"}
        </h2>
        {loading ? (
          <div className="loading">Loading complaints...</div>
        ) : displayedComplaints.length > 0 ? (
          <div className="complaints-table-container">
            <table className="complaints-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Status</th>
                  <th>Location</th>
                  <th>Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {displayedComplaints.map(complaint => (
                  <tr key={complaint.id} className={`status-${complaint.status}`}>
                    <td className="complaint-id">#{complaint.id}</td>
                    <td className="complaint-title">{complaint.title}</td>
                    <td className="complaint-category">
                      <span className="badge">{complaint.category || "Other"}</span>
                    </td>
                    <td className="complaint-status">
                      <span className={`status-badge status-${complaint.status}`}>
                        {complaint.status}
                      </span>
                    </td>
                    <td className="complaint-location">{complaint.location}</td>
                    <td className="complaint-date">
                      {complaint.createdAt ? new Date(complaint.createdAt).toLocaleDateString() : "N/A"}
                    </td>
                    <td className="complaint-action">
                      <button 
                        className="view-btn"
                        onClick={() => navigate(`/admin/complaints/${complaint.id}`)}
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="empty-state">
            {selectedCategory ? `No complaints in ${selectedCategory}` : "No complaints found"}
          </p>
        )}
        {error && (
          <div className="info-banner">
            ℹ️ Could not load live data: {error}
          </div>
        )}
      </section>
    </div>
  );
}
