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
    private Long userId;
    private boolean escalated = false;
    private String sentiment;
    private Double confidenceScore;
    private String remarks;
}
