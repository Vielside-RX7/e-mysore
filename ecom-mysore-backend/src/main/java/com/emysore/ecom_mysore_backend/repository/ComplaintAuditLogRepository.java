package com.emysore.ecom_mysore_backend.repository;

import com.emysore.ecom_mysore_backend.model.ComplaintAuditLog;
import com.emysore.ecom_mysore_backend.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAuditLogRepository extends JpaRepository<ComplaintAuditLog, Long> {
    List<ComplaintAuditLog> findByComplaintOrderByTimestampDesc(Complaint complaint);
    List<ComplaintAuditLog> findByComplaintIdOrderByTimestampDesc(Long complaintId);
}