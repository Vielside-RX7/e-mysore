package com.emysore.ecom_mysore_backend.repository;

import com.emysore.ecom_mysore_backend.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStatus(String status);
    List<Complaint> findByStatusAndEscalated(String status, boolean escalated);
    List<Complaint> findByStatusAndCreatedAtBefore(String status, java.time.LocalDateTime createdAt);
}
