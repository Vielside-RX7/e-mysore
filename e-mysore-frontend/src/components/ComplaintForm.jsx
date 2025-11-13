import React, { useState } from "react";
import axios from "axios";
import "./ComplaintForm.css";

const ComplaintForm = () => {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState("Roads");
  const [location, setLocation] = useState("");
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState(null);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    setImage(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const formData = new FormData();
      formData.append("title", title);
      formData.append("description", description);
      formData.append("category", category);
      formData.append("location", location);
      if (image) formData.append("image", image);

      const token = localStorage.getItem("emysore_token");
      const headers = {};
      if (token) headers["Authorization"] = `Bearer ${token}`;

      await axios.post(
        "http://localhost:8080/api/complaints/with-image",
        formData,
        { headers }
      );

      setSuccess(true);
      setTitle("");
      setDescription("");
      setCategory("Roads");
      setLocation("");
      setImage(null);

      setTimeout(() => setSuccess(false), 3000);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || "Failed to submit complaint. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ios-complaint-container">
      {/* Header */}
      <div className="complaint-header">
        <h1>Register a Complaint</h1>
        <p className="complaint-subtitle">Help us improve Mysore by reporting issues</p>
      </div>

      {/* Form Card */}
      <form className="ios-complaint-form" onSubmit={handleSubmit}>
        {/* Title Field */}
        <div className="ios-form-group">
          <label className="ios-label">Title</label>
          <input
            type="text"
            className="ios-input"
            placeholder="e.g. Pothole on MG Road"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>

        {/* Description Field */}
        <div className="ios-form-group">
          <label className="ios-label">Description</label>
          <textarea
            className="ios-textarea"
            placeholder="Describe the issue in detail..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={5}
            required
          />
        </div>

        {/* Category Field */}
        <div className="ios-form-group">
          <label className="ios-label">Category</label>
          <select
            className="ios-select"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            required
          >
            <option value="Roads">Roads & Pavements</option>
            <option value="Water">Water Supply</option>
            <option value="Waste">Waste Management</option>
            <option value="Street Light">Street Lighting</option>
            <option value="Drainage">Drainage</option>
            <option value="Other">Other</option>
          </select>
        </div>

        {/* Location Field */}
        <div className="ios-form-group">
          <label className="ios-label">Location</label>
          <input
            type="text"
            className="ios-input"
            placeholder="e.g. 5th Cross, Mysore"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            required
          />
        </div>

        {/* Image Upload */}
        <div className="ios-form-group">
          <label className="ios-label">Upload Image (optional)</label>
          <div className="ios-file-input-wrapper">
            <input
              type="file"
              accept="image/*"
              onChange={handleImageChange}
              className="ios-file-input"
              id="image-upload"
            />
            <label htmlFor="image-upload" className="ios-file-label">
              <span className="file-icon">📷</span>
              <span className="file-text">
                {image ? `✓ ${image.name}` : "Choose image or drag here"}
              </span>
            </label>
          </div>

          {/* Image Preview */}
          {image && (
            <div className="ios-image-preview">
              <img
                src={URL.createObjectURL(image)}
                alt="Preview"
                className="preview-image"
              />
              <button
                type="button"
                className="remove-image-btn"
                onClick={() => setImage(null)}
              >
                ✕
              </button>
            </div>
          )}
        </div>

        {/* Success Message */}
        {success && (
          <div className="ios-success-message">
            <span>✓</span> Complaint submitted successfully!
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="ios-error-message">
            <span>⚠️</span> {error}
          </div>
        )}

        {/* Submit Button */}
        <button
          type="submit"
          disabled={loading}
          className="ios-button ios-button-primary ios-submit-btn"
        >
          {loading ? "Submitting..." : "Submit Complaint"}
        </button>
      </form>
    </div>
  );
};

export default ComplaintForm;
