import React from "react";
import { Navigate } from "react-router-dom";

// Props: children, requireRole (optional string like 'ADMIN' or 'CITIZEN')
export default function ProtectedRoute({ children, requireRole }) {
  const token = localStorage.getItem("emysore_token");
  const role = localStorage.getItem("emysore_role");

  if (!token) {
    // not authenticated
    return <Navigate to="/login" replace />;
  }

  if (requireRole && role !== requireRole) {
    // authenticated but wrong role
    return <Navigate to="/login" replace />;
  }

  return children;
}
