package com.emysore.ecom_mysore_backend.repository;

import com.emysore.ecom_mysore_backend.model.CityService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityServiceRepository extends JpaRepository<CityService, Long> {
    List<CityService> findByCategory(String category);
}