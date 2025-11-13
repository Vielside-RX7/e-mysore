package com.emysore.ecom_mysore_backend.repository;

import com.emysore.ecom_mysore_backend.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
}
