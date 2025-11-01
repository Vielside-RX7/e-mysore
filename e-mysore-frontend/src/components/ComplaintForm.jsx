import React, { useState } from "react";
import axios from "axios";
import "./ComplaintForm.css"; // Import the CSS file

const ComplaintForm = () => {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("Roads");
  const [location, setLocation] = useState("");
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const formData = new FormData();
      formData.append("title", title);
      formData.append("description", description);
      formData.append("category", category);
      formData.append("location", location);
      if (image) formData.append("image", image);

      await axios.post(
        "http://localhost:8080/api/complaints/with-image",
        formData,
        { headers: { "Content-Type": "multipart/form-data" } }
      );

      alert("✅ Complaint submitted successfully!");
      setTitle("");
      setDescription("");
      setCategory("Roads");
      setLocation("");
      setImage(null);
    } catch (error) {
      console.error(error);
      alert("❌ Error submitting complaint");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="complaint-container">
      <h2>📝 Register a Complaint</h2>
      <form onSubmit={handleSubmit} className="complaint-form">
        <label>Title</label>
        <input
          type="text"
          placeholder="Enter title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />

        <label>Description</label>
        <textarea
          placeholder="Describe your issue..."
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={4}
          required
        />

        <label>Category</label>
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          required
        >
          <option>Roads</option>
          <option>Water</option>
          <option>Waste</option>
          <option>Street Light</option>
        </select>

        <label>Location</label>
        <input
          type="text"
          placeholder="e.g. 5th Cross, Mysore"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
          required
        />

        <label>Upload Image (optional)</label>
        <input
          key={image ? image.name : "file"}
          type="file"
          accept="image/*"
          onChange={(e) => setImage(e.target.files[0])}
        />

        {image && (
          <img
            src={URL.createObjectURL(image)}
            alt="Preview"
            className="preview-image"
          />
        )}

        <button type="submit" disabled={loading}>
          {loading ? "Submitting..." : "Submit Complaint"}
        </button>
      </form>
    </div>
  );
};

export default ComplaintForm;
