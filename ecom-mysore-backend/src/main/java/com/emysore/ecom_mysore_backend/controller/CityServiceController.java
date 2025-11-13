package com.emysore.ecom_mysore_backend.controller;

import com.emysore.ecom_mysore_backend.model.CityService;
import com.emysore.ecom_mysore_backend.service.CityServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class CityServiceController {

    @Autowired
    private CityServiceService cityServiceService;

    @GetMapping
    public List<CityService> getAllServices() {
        return cityServiceService.getAllServices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityService> getServiceById(@PathVariable Long id) {
        return cityServiceService.getServiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public List<CityService> getServicesByCategory(@PathVariable String category) {
        return cityServiceService.getServicesByCategory(category);
    }

    @PostMapping
    public CityService createService(@RequestBody CityService service) {
        return cityServiceService.createService(service);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityService> updateService(
            @PathVariable Long id,
            @RequestBody CityService serviceDetails) {
        try {
            CityService updatedService = cityServiceService.updateService(id, serviceDetails);
            return ResponseEntity.ok(updatedService);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        try {
            cityServiceService.deleteService(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}