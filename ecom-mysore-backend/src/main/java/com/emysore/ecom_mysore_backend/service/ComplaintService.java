package com.emysore.ecom_mysore_backend.service;

import com.emysore.ecom_mysore_backend.model.*;
import com.emysore.ecom_mysore_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.util.*;
import java.time.LocalDateTime;
import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintAuditLogRepository auditLogRepository;

    @Autowired
    private MLService mlService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.emysore.ecom_mysore_backend.repository.DepartmentRepository departmentRepository;

    @Autowired(required = false)
    private S3Client s3Client;

    @Value("${aws.s3.bucket:local}")
    private String bucketName;

    @Value("${aws.s3.region:local}")
    private String region;

    @Transactional
    public Complaint createComplaint(Complaint complaint, User user, MultipartFile image) throws IOException {
        complaint.setUser(user);
        complaint.setCreatedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());
        complaint.setStatus("PENDING");

        // Try to enrich complaint with ML predictions
        try {
            mlService.enrichComplaint(complaint);
        } catch (Exception e) {
            // Log error but continue
        }

        // Handle image upload if present
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadImage(image, user.getId());
            complaint.setImageUrl(imageUrl);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Create audit log
        ComplaintAuditLog auditLog = new ComplaintAuditLog();
        auditLog.setComplaint(savedComplaint);
        auditLog.setUser(user);
        auditLog.setAction("CREATED");
        auditLog.setOldValue("");
        auditLog.setNewValue("PENDING");
        auditLog.setComment("Complaint created");
        auditLogRepository.save(auditLog);

        // Send notification
        notificationService.createNotification(
            user,
            "Complaint Filed Successfully",
            "Your complaint has been filed with ID #" + savedComplaint.getId(),
            Notification.NotificationType.COMPLAINT_CREATED
        );

        // If complaint assigned to a department on create, notify department contact
        try {
            String assigned = savedComplaint.getAssignedDept();
            if (assigned != null && !assigned.isEmpty()) {
                Department dept = departmentRepository.findByName(assigned);
                if (dept != null) {
                    String deptMsg = "New complaint #" + savedComplaint.getId() + " assigned to your department. Please take action.";
                    notificationService.sendDirectContactNotification(dept.getContactEmail(), dept.getPhone(), "New Complaint Assigned", deptMsg);
                }
            }
        } catch (Exception ignored) {}

        return savedComplaint;
    }

    @Transactional
    public Complaint updateStatus(Long id, String newStatus, User officer, String remarks) {
        Complaint complaint = complaintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Complaint not found"));

        String oldStatus = complaint.getStatus();
        complaint.setStatus(newStatus);
        complaint.setRemarks(remarks);
        complaint.setUpdatedAt(LocalDateTime.now());
        
        Complaint savedComplaint = complaintRepository.save(complaint);

        // Create audit log
        ComplaintAuditLog auditLog = new ComplaintAuditLog();
        auditLog.setComplaint(savedComplaint);
        auditLog.setUser(officer);
        auditLog.setAction("STATUS_UPDATED");
        auditLog.setOldValue(oldStatus);
        auditLog.setNewValue(newStatus);
        auditLog.setComment(remarks);
        auditLogRepository.save(auditLog);

        // Notify the citizen
        notificationService.createNotification(
            complaint.getUser(),
            "Complaint Status Updated",
            "Your complaint #" + id + " status has been updated to " + newStatus,
            Notification.NotificationType.COMPLAINT_UPDATED
        );

        // Notify assigned department contact if present
        try {
            String assigned = complaint.getAssignedDept();
            if (assigned != null && !assigned.isEmpty()) {
                Department dept = departmentRepository.findByName(assigned);
                if (dept != null) {
                    String deptMsg = "Complaint #" + id + " has been updated to status: " + newStatus + ". Please review.";
                    notificationService.sendDirectContactNotification(dept.getContactEmail(), dept.getPhone(), "Complaint Update - " + id, deptMsg);
                }
            }
        } catch (Exception ignored) {}

        return savedComplaint;
    }

    @Transactional
    public Complaint escalateComplaint(Long id, Boolean escalate, User officer) {
        Complaint complaint = complaintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Complaint not found"));

        boolean oldEscalated = complaint.isEscalated();
        complaint.setEscalated(escalate);
        complaint.setUpdatedAt(LocalDateTime.now());
        
        Complaint savedComplaint = complaintRepository.save(complaint);

        // Create audit log
        ComplaintAuditLog auditLog = new ComplaintAuditLog();
        auditLog.setComplaint(savedComplaint);
        auditLog.setUser(officer);
        auditLog.setAction("ESCALATED");
        auditLog.setOldValue(String.valueOf(oldEscalated));
        auditLog.setNewValue(String.valueOf(escalate));
        auditLog.setComment("Complaint manually escalated by admin");
        auditLogRepository.save(auditLog);

        // Notify the citizen
        if (escalate) {
            notificationService.createNotification(
                complaint.getUser(),
                "Complaint Escalated",
                "Your complaint #" + id + " has been escalated to high priority",
                Notification.NotificationType.COMPLAINT_ESCALATED
            );

            // Notify assigned department contact if present
            try {
                String assigned = complaint.getAssignedDept();
                if (assigned != null && !assigned.isEmpty()) {
                    Department dept = departmentRepository.findByName(assigned);
                    if (dept != null) {
                        String deptMsg = "URGENT: Complaint #" + id + " has been escalated to high priority. Immediate action required.";
                        notificationService.sendDirectContactNotification(dept.getContactEmail(), dept.getPhone(), "Complaint Escalated - " + id, deptMsg);
                    }
                }
            } catch (Exception ignored) {}
        }

        return savedComplaint;
    }

    private String uploadImage(MultipartFile file, Long userId) throws IOException {
        String key = String.format("complaints/%d/%s-%s", 
            userId,
            UUID.randomUUID().toString(),
            file.getOriginalFilename()
        );
        if (s3Client != null) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                file.getInputStream(), 
                file.getSize()
            ));

            return String.format("https://%s.s3.%s.amazonaws.com/%s", 
                bucketName, region, key);
        } else {
            // Fallback: write to local uploads directory
            java.nio.file.Path uploadsDir = java.nio.file.Paths.get("uploads/complaints/" + userId);
            java.nio.file.Files.createDirectories(uploadsDir);
            java.nio.file.Path out = uploadsDir.resolve(UUID.randomUUID().toString() + "-" + file.getOriginalFilename());
            try (java.io.InputStream in = file.getInputStream()) {
                java.nio.file.Files.copy(in, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return out.toAbsolutePath().toString();
        }
    }

    @Transactional(readOnly = true)
    public Page<Complaint> getComplaints(String status, String category, Boolean escalated, Pageable pageable) {
        // TODO: Implement dynamic filtering based on parameters
        return complaintRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<ComplaintAuditLog> getComplaintAuditLogs(Long complaintId) {
        return auditLogRepository.findByComplaintIdOrderByTimestampDesc(complaintId);
    }

    @Transactional(readOnly = true)
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Complaint not found"));
    }

    @Transactional(readOnly = true)
    public List<Complaint> searchComplaints(String searchTerm) {
        // TODO: Implement full-text search
        return complaintRepository.findAll();
    }

    @Transactional
    public void escalateOverdue() {
        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(3); // Complaints older than 3 days
        List<Complaint> overdueComplaints = complaintRepository.findByStatusAndCreatedAtBefore("PENDING", thresholdTime);
        
        for (Complaint complaint : overdueComplaints) {
            complaint.setEscalated(true);
            complaint.setUpdatedAt(LocalDateTime.now());
            complaintRepository.save(complaint);

            // Create audit log
            ComplaintAuditLog auditLog = new ComplaintAuditLog();
            auditLog.setComplaint(complaint);
            auditLog.setAction("ESCALATED");
            auditLog.setOldValue("PENDING");
            auditLog.setNewValue("ESCALATED");
            auditLog.setComment("Complaint automatically escalated due to overdue status");
            auditLogRepository.save(auditLog);

            // Notify relevant parties
            notificationService.createNotification(
                complaint.getUser(),
                "Complaint Escalated",
                "Your complaint #" + complaint.getId() + " has been escalated due to delayed response",
                Notification.NotificationType.COMPLAINT_ESCALATED
            );

            // Notify department if assigned
            try {
                String assigned = complaint.getAssignedDept();
                if (assigned != null && !assigned.isEmpty()) {
                    Department dept = departmentRepository.findByName(assigned);
                    if (dept != null) {
                        String deptMsg = "URGENT: Complaint #" + complaint.getId() + " has been escalated due to delayed response. Immediate action required.";
                        notificationService.sendDirectContactNotification(dept.getContactEmail(), dept.getPhone(), "Complaint Escalated - " + complaint.getId(), deptMsg);
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    @Transactional
    public void addComment(Long complaintId, User user, String comment) {
        Complaint complaint = getComplaintById(complaintId);
        
        ComplaintAuditLog auditLog = new ComplaintAuditLog();
        auditLog.setComplaint(complaint);
        auditLog.setUser(user);
        auditLog.setAction("COMMENT_ADDED");
        auditLog.setOldValue("");
        auditLog.setNewValue("");
        auditLog.setComment(comment);
        auditLogRepository.save(auditLog);

        // Notify the complaint owner if the comment is from someone else
        if (!user.getId().equals(complaint.getUser().getId())) {
            notificationService.createNotification(
                complaint.getUser(),
                "New Comment on Complaint #" + complaintId,
                "A new comment has been added to your complaint",
                Notification.NotificationType.COMPLAINT_UPDATED
            );
        }
    }
}
