package com.emysore.ecom_mysore_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String category;
    private String status = "Pending";
    private String urgency = "Standard";
    private String location;
    private String imageUrl;
    private String assignedDept;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime deadline;

    // New fields
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean escalated = false;
    private String sentiment;
    private Double confidenceScore;
    private String remarks;

    // Getters and setters for User
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getLastModified() { return updatedAt; }
    public void setLastModified(LocalDateTime lastModified) { this.updatedAt = lastModified; }
}
