package com.emysore.ecom_mysore_backend.service;

import com.emysore.ecom_mysore_backend.model.CityService;
import com.emysore.ecom_mysore_backend.repository.CityServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CityServiceService {

    @Autowired
    private CityServiceRepository cityServiceRepository;

    public List<CityService> getAllServices() {
        return cityServiceRepository.findAll();
    }

    public Optional<CityService> getServiceById(Long id) {
        return cityServiceRepository.findById(id);
    }

    public List<CityService> getServicesByCategory(String category) {
        return cityServiceRepository.findByCategory(category);
    }

    public CityService createService(CityService service) {
        return cityServiceRepository.save(service);
    }

    public CityService updateService(Long id, CityService serviceDetails) {
        CityService service = cityServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));

        service.setName(serviceDetails.getName());
        service.setDescription(serviceDetails.getDescription());
        service.setCategory(serviceDetails.getCategory());
        service.setPhone(serviceDetails.getPhone());
        service.setEmail(serviceDetails.getEmail());
        service.setAddress(serviceDetails.getAddress());

        return cityServiceRepository.save(service);
    }

    public void deleteService(Long id) {
        CityService service = cityServiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + id));
        cityServiceRepository.delete(service);
    }
}