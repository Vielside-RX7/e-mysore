import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import "./AdminComplaintDetail.css";

export default function AdminComplaintDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [complaint, setComplaint] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [remarks, setRemarks] = useState("");
  const [selectedStatus, setSelectedStatus] = useState("");

  useEffect(() => {
    fetchComplaintDetails();
  }, [id]);

  const fetchComplaintDetails = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem("emysore_token");
      const headers = { Accept: "application/json" };
      if (token) headers["Authorization"] = `Bearer ${token}`;

      const res = await fetch(`http://localhost:8080/api/complaints/${id}`, {
        headers
      });
      if (!res.ok) throw new Error(`Failed to fetch complaint: HTTP ${res.status}`);

      const data = await res.json();
      setComplaint(data);
      setSelectedStatus(data.status);
      setError(null);
    } catch (e) {
      console.error("Error fetching complaint:", e);
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleEscalate = async () => {
    if (!complaint) return;
    try {
      setActionLoading(true);
      const token = localStorage.getItem("emysore_token");
      const res = await fetch(`http://localhost:8080/api/complaints/${id}/escalate`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ escalated: true })
      });

      if (!res.ok) throw new Error(`Failed to escalate: HTTP ${res.status}`);
      
      setComplaint(prev => ({ ...prev, escalated: true }));
      alert("✓ Complaint escalated successfully");
    } catch (e) {
      console.error("Error escalating complaint:", e);
      alert(`Error: ${e.message}`);
    } finally {
      setActionLoading(false);
    }
  };

  const handleStatusUpdate = async () => {
    if (!complaint) return;
    try {
      setActionLoading(true);
      const token = localStorage.getItem("emysore_token");
      const res = await fetch(`http://localhost:8080/api/complaints/${id}/status`, {
        method: "PATCH",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          status: selectedStatus,
          remarks: remarks
        })
      });

      if (!res.ok) throw new Error(`Failed to update status: HTTP ${res.status}`);

      const updated = await res.json();
      setComplaint(updated);
      setRemarks("");
      alert("✓ Status updated successfully");
    } catch (e) {
      console.error("Error updating status:", e);
      alert(`Error: ${e.message}`);
    } finally {
      setActionLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "PENDING":
        return "#FF9500";
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

  const getCategoryBg = (category) => {
    const colors = {
      Infrastructure: "#007AFF",
      Water: "#5AC8FA",
      Roads: "#FFA500",
      Lighting: "#FFD700",
      Sanitation: "#50C878",
      Safety: "#FF6B6B",
      Other: "#A9A9A9"
    };
    return colors[category] || "#A9A9A9";
  };

  if (loading) {
    return (
      <div className="admin-detail-container">
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading complaint details...</p>
        </div>
      </div>
    );
  }

  if (error || !complaint) {
    return (
      <div className="admin-detail-container">
        <button onClick={() => navigate("/admin/dashboard")} className="back-btn">
          ← Back
        </button>
        <div className="error-box">
          <p>❌ {error || "Complaint not found"}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-detail-container">
      {/* Header */}
      <div className="detail-header">
        <button onClick={() => navigate("/admin/dashboard")} className="back-btn">
          ← Back to Dashboard
        </button>
        <h1>Complaint Details</h1>
        <p className="complaint-id">ID: #{complaint.id}</p>
      </div>

      {/* Main Content */}
      <div className="detail-content">
        {/* Left Panel - Complaint Info */}
        <div className="detail-left-panel">
          <section className="detail-section">
            <h2 className="section-title">Complaint Information</h2>
            
            <div className="info-group">
              <label>Title</label>
              <div className="info-value large-text">{complaint.title}</div>
            </div>

            <div className="info-row">
              <div className="info-group">
                <label>Category</label>
                <div 
                  className="info-value badge"
                  style={{ backgroundColor: getCategoryBg(complaint.category) }}
                >
                  {complaint.category || "Other"}
                </div>
              </div>
              <div className="info-group">
                <label>Location</label>
                <div className="info-value">📍 {complaint.location}</div>
              </div>
            </div>

            <div className="info-group">
              <label>Description</label>
              <div className="info-value description-box">{complaint.description}</div>
            </div>

            {complaint.imageUrl && (
              <div className="info-group">
                <label>Attached Image</label>
                <img src={complaint.imageUrl} alt="complaint" className="complaint-image" />
              </div>
            )}

            <div className="info-row">
              <div className="info-group">
                <label>Created Date</label>
                <div className="info-value">
                  📅 {new Date(complaint.createdAt).toLocaleDateString()} 
                  {" at "} 
                  {new Date(complaint.createdAt).toLocaleTimeString()}
                </div>
              </div>
              <div className="info-group">
                <label>Last Updated</label>
                <div className="info-value">
                  {new Date(complaint.updatedAt).toLocaleDateString()}
                </div>
              </div>
            </div>

            {complaint.user && (
              <div className="info-group">
                <label>Submitted By</label>
                <div className="info-value">
                  👤 {complaint.user.username}
                  {complaint.user.email && <span> ({complaint.user.email})</span>}
                </div>
              </div>
            )}
          </section>

          {/* Analytics */}
          <section className="detail-section">
            <h2 className="section-title">Analysis</h2>
            
            {complaint.sentiment && (
              <div className="info-group">
                <label>Sentiment</label>
                <div className="info-value">{complaint.sentiment}</div>
              </div>
            )}

            {complaint.confidenceScore !== null && (
              <div className="info-group">
                <label>Confidence Score</label>
                <div className="progress-bar">
                  <div 
                    className="progress-fill"
                    style={{ width: `${complaint.confidenceScore * 100}%` }}
                  ></div>
                </div>
                <span className="confidence-text">{(complaint.confidenceScore * 100).toFixed(0)}%</span>
              </div>
            )}

            {complaint.remarks && (
              <div className="info-group">
                <label>Admin Remarks</label>
                <div className="info-value remarks-box">{complaint.remarks}</div>
              </div>
            )}
          </section>
        </div>

        {/* Right Panel - Actions */}
        <div className="detail-right-panel">
          {/* Status Section */}
          <section className="action-section">
            <h3 className="section-title">Status Management</h3>
            
            <div className="current-status">
              <label>Current Status</label>
              <div 
                className="status-badge large"
                style={{ backgroundColor: getStatusColor(complaint.status) }}
              >
                {complaint.status}
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="status-select">Update Status</label>
              <select
                id="status-select"
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="status-select"
              >
                <option value="PENDING">Pending</option>
                <option value="OPEN">Open</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="RESOLVED">Resolved</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="remarks">Add Remarks</label>
              <textarea
                id="remarks"
                value={remarks}
                onChange={(e) => setRemarks(e.target.value)}
                placeholder="Add notes about this complaint..."
                className="remarks-input"
                rows="4"
              />
            </div>

            <button
              onClick={handleStatusUpdate}
              disabled={actionLoading}
              className="btn btn-primary"
            >
              {actionLoading ? "Updating..." : "Update Status"}
            </button>
          </section>

          {/* Escalation Section */}
          <section className="action-section">
            <h3 className="section-title">Escalation</h3>
            
            <div className="escalation-status">
              <div className={`escalation-indicator ${complaint.escalated ? "escalated" : ""}`}>
                <span className="status-dot"></span>
                <span>{complaint.escalated ? "⚠️ Escalated" : "Normal Priority"}</span>
              </div>
            </div>

            <button
              onClick={handleEscalate}
              disabled={actionLoading || complaint.escalated}
              className={`btn ${complaint.escalated ? "btn-disabled" : "btn-danger"}`}
            >
              {complaint.escalated ? "✓ Already Escalated" : "⚡ Escalate Now"}
            </button>
          </section>

          {/* Metadata */}
          <section className="action-section">
            <h3 className="section-title">Metadata</h3>
            
            <div className="metadata-grid">
              <div className="metadata-item">
                <label>Urgency</label>
                <div className="metadata-value">{complaint.urgency || "MEDIUM"}</div>
              </div>
              <div className="metadata-item">
                <label>Assigned Dept</label>
                <div className="metadata-value">{complaint.assignedDept || "Unassigned"}</div>
              </div>
              {complaint.deadline && (
                <div className="metadata-item">
                  <label>Deadline</label>
                  <div className="metadata-value">{new Date(complaint.deadline).toLocaleDateString()}</div>
                </div>
              )}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
