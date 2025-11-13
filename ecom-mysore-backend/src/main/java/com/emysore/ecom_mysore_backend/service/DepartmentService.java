package com.emysore.ecom_mysore_backend.service;

import com.emysore.ecom_mysore_backend.model.Department;
import com.emysore.ecom_mysore_backend.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository repo;

    public DepartmentService(DepartmentRepository repo) { this.repo = repo; }

    public Department create(Department d) { return repo.save(d); }
    public Department update(Long id, Department d) {
        return repo.findById(id).map(existing -> {
            existing.setName(d.getName());
            existing.setContactEmail(d.getContactEmail());
            existing.setPhone(d.getPhone());
            return repo.save(existing);
        }).orElse(null);
    }
    public void delete(Long id) { repo.deleteById(id); }
    public Department get(Long id) { return repo.findById(id).orElse(null); }
    public List<Department> list() { return repo.findAll(); }
    public Department findByName(String name) { return repo.findByName(name); }
}
