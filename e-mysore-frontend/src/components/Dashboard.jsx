import React, { useEffect, useState } from "react";
import "./Dashboard.css";

function Dashboard({ user }) {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchComplaints = async () => {
      try {
        const res = await fetch("http://localhost:8080/api/complaints");
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        // Handle both paginated and non-paginated responses
        const complaintList = data.content || data;
        setComplaints(Array.isArray(complaintList) ? complaintList : []);
        setError(null);
      } catch (e) {
        // fallback: mock data so the dashboard still shows something
        console.warn("Failed to fetch complaints, using mock data:", e.message);
        setError(e.message);
        setComplaints([
          { id: 1, title: "Pothole on MG Road", status: "OPEN", category: "Roads", location: "MG Road", createdAt: "2025-10-30" },
          { id: 2, title: "Overflowing drain", status: "IN_PROGRESS", category: "Water", location: "Vinoba Road", createdAt: "2025-10-29" },
          { id: 3, title: "Street light not working", status: "RESOLVED", category: "Lighting", location: "KRS Road", createdAt: "2025-10-28" }
        ]);
      } finally {
        setLoading(false);
      }
    };

    fetchComplaints();
  }, []);

  const total = complaints.length;
  const open = complaints.filter(c => c.status === "OPEN").length;
  const escalated = complaints.filter(c => c.escalated).length;

  const getStatusColor = (status) => {
    switch (status) {
      case "OPEN":
        return "#FF9500";
      case "IN_PROGRESS":
        return "#0A84FF";
      case "RESOLVED":
        return "#34C759";
      default:
        return "#8E8E93";
    }
  };

  return (
    <div className="ios-dashboard">
      {/* Header */}
      <div className="ios-dashboard-header">
        <div className="header-content">
          <h1 className="dashboard-title">Dashboard</h1>
          <p className="dashboard-subtitle">City-centric civic engagement and smart governance</p>
          {user && <p className="dashboard-welcome">Welcome back, <strong>{user}</strong></p>}
        </div>
      </div>

      {/* Summary Cards */}
      <section className="ios-summary-cards">
        <div className="ios-stat-card">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <div className="stat-value">{loading ? "..." : total}</div>
            <div className="stat-label">Total Complaints</div>
          </div>
        </div>
        <div className="ios-stat-card">
          <div className="stat-icon">🔔</div>
          <div className="stat-content">
            <div className="stat-value">{loading ? "..." : open}</div>
            <div className="stat-label">Open</div>
          </div>
        </div>
        <div className="ios-stat-card">
          <div className="stat-icon">⚡</div>
          <div className="stat-content">
            <div className="stat-value">{loading ? "..." : escalated}</div>
            <div className="stat-label">Escalated</div>
          </div>
        </div>
      </section>

      {/* Recent Complaints */}
      <section className="ios-recent-section">
        <div className="section-header">
          <h2>Recent Complaints</h2>
          <p className="section-subtitle">Latest submissions</p>
        </div>

        {loading && (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loading complaints...</p>
          </div>
        )}

        {!loading && complaints.length > 0 && (
          <div className="ios-complaint-list">
            {complaints.slice(0, 5).map((c) => (
              <div key={c.id} className="ios-complaint-card">
                <div className="complaint-card-header">
                  <h3 className="complaint-title">{c.title}</h3>
                  <span
                    className="complaint-status"
                    style={{
                      backgroundColor: getStatusColor(c.status),
                      color: "#ffffff"
                    }}
                  >
                    {c.status || "UNKNOWN"}
                  </span>
                </div>
                <div className="complaint-card-meta">
                  <span className="meta-item">📍 {c.location}</span>
                  <span className="meta-item">🏷️ {c.category}</span>
                  <span className="meta-item">📅 {c.createdAt}</span>
                </div>
              </div>
            ))}
          </div>
        )}

        {!loading && complaints.length === 0 && (
          <div className="empty-state">
            <p>No complaints yet</p>
          </div>
        )}

        {error && (
          <div className="ios-info-message">
            <span>ℹ️</span> Could not load live data; showing mock results.
          </div>
        )}
      </section>
    </div>
  );
}

export default Dashboard;
