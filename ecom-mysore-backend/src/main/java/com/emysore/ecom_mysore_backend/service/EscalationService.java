package com.emysore.ecom_mysore_backend.service;

import com.emysore.ecom_mysore_backend.model.Complaint;
import com.emysore.ecom_mysore_backend.model.Notification;
import com.emysore.ecom_mysore_backend.model.Role;
import com.emysore.ecom_mysore_backend.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class EscalationService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private com.emysore.ecom_mysore_backend.repository.DepartmentRepository departmentRepository;

    // Check for escalations every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkAndEscalateComplaints() {
        List<Complaint> pendingComplaints = complaintRepository.findByStatus("PENDING");
        LocalDateTime now = LocalDateTime.now();

        for (Complaint complaint : pendingComplaints) {
            long daysElapsed = ChronoUnit.DAYS.between(complaint.getCreatedAt(), now);
            
            if (daysElapsed >= 7 && !complaint.isEscalated()) {
                escalateComplaint(complaint);
            }
        }
    }

    @Transactional
    public void escalateComplaint(Complaint complaint) {
        // Update complaint status
        complaint.setEscalated(true);
        complaint.setStatus("ESCALATED");
        complaint.setLastModified(LocalDateTime.now());
        
    // Save the updated complaint
    complaintRepository.save(complaint);

        // Notify citizen
        notificationService.createNotification(
            complaint.getUser(),
            "Complaint Escalated",
            "Your complaint #" + complaint.getId() + " has been escalated due to delay in resolution.",
            Notification.NotificationType.COMPLAINT_ESCALATED
        );

        // Notify department admin
        try {
            String assigned = complaint.getAssignedDept();
            if (assigned != null && !assigned.isEmpty()) {
                var dept = departmentRepository.findByName(assigned);
                if (dept != null) {
                    String deptMsg = "Complaint #" + complaint.getId() + " has been escalated. Immediate action required.";
                    notificationService.sendDirectContactNotification(dept.getContactEmail(), dept.getPhone(), "Complaint Escalated - " + complaint.getId(), deptMsg);
                }
            }
        } catch (Exception ignored) {}
    }

    @Transactional
    public boolean shouldEscalate(Complaint complaint) {
        if (complaint.isEscalated() || !"PENDING".equals(complaint.getStatus())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysElapsed = ChronoUnit.DAYS.between(complaint.getCreatedAt(), now);
        
        return daysElapsed >= 7;
    }
}