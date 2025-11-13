package com.emysore.ecom_mysore_backend.repository;

import com.emysore.ecom_mysore_backend.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByName(String name);
}
