import React, { useEffect, useState } from "react";

export default function AdminComplaints() {
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const token = localStorage.getItem("emysore_token");

  useEffect(() => {
    fetchComplaints();
  }, []);

  const fetchComplaints = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch("http://localhost:8080/api/complaints");
      const data = await res.json();
      setComplaints(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (id, status, remarks) => {
    try {
      const body = { status, remarks };
      const headers = { "Content-Type": "application/json" };
      if (token) headers["Authorization"] = `Bearer ${token}`;

      const res = await fetch(`http://localhost:8080/api/complaints/${id}/status`, {
        method: "PATCH",
        headers,
        body: JSON.stringify(body),
      });
      if (!res.ok) throw new Error("Failed to update status");
      await fetchComplaints();
      alert("Status updated");
    } catch (err) {
      alert("Error: " + err.message);
    }
  };

  if (loading) return <div style={{maxWidth:980, margin:'24px auto'}}>Loading complaints...</div>;
  if (error) return <div style={{maxWidth:980, margin:'24px auto', color:'red'}}>Error: {error}</div>;

  return (
    <div style={{maxWidth:980, margin:'24px auto', padding:'0 16px'}}>
      <h2>All Complaints</h2>
      {complaints.length === 0 && <div>No complaints yet.</div>}
      <table style={{width:'100%', borderCollapse:'collapse'}}>
        <thead>
          <tr>
            <th style={{borderBottom:'1px solid #ddd', textAlign:'left'}}>ID</th>
            <th style={{borderBottom:'1px solid #ddd', textAlign:'left'}}>Title</th>
            <th style={{borderBottom:'1px solid #ddd', textAlign:'left'}}>Category</th>
            <th style={{borderBottom:'1px solid #ddd', textAlign:'left'}}>Status</th>
            <th style={{borderBottom:'1px solid #ddd', textAlign:'left'}}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {complaints.map(c => (
            <tr key={c.id}>
              <td style={{padding:'8px 4px'}}>{c.id}</td>
              <td style={{padding:'8px 4px'}}>{c.title}</td>
              <td style={{padding:'8px 4px'}}>{c.category}</td>
              <td style={{padding:'8px 4px'}}>{c.status || 'New'}</td>
              <td style={{padding:'8px 4px'}}>
                <select id={`status-${c.id}`} defaultValue={c.status || 'New'}>
                  <option>New</option>
                  <option>In Progress</option>
                  <option>Resolved</option>
                  <option>Closed</option>
                </select>
                <input id={`remarks-${c.id}`} placeholder="remarks" style={{marginLeft:8}} />
                <button style={{marginLeft:8}} onClick={() => {
                  const s = document.getElementById(`status-${c.id}`).value;
                  const r = document.getElementById(`remarks-${c.id}`).value;
                  updateStatus(c.id, s, r);
                }}>Update</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
