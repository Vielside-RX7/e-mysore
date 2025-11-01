package com.emysore.ecom_mysore_backend.controller;

import com.emysore.ecom_mysore_backend.model.Complaint;
import com.emysore.ecom_mysore_backend.service.ComplaintService;
import com.emysore.ecom_mysore_backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private FileStorageService fileStorageService;

    // JSON-only endpoint
    @PostMapping
    public Complaint registerComplaint(@RequestBody Complaint complaint) {
        return complaintService.createComplaint(complaint);
    }

    // Multipart endpoint for frontend form with optional image
    @PostMapping("/with-image")
    public Complaint registerComplaintWithImage(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        Complaint complaint = new Complaint();
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setCategory(category);
        complaint.setLocation(location);

        if (image != null && !image.isEmpty()) {
            String imageUrl = fileStorageService.uploadFile(image);
            complaint.setImageUrl(imageUrl);
        }

        return complaintService.createComplaint(complaint);
    }

    @GetMapping
    public List<Complaint> getAllComplaints() {
        return complaintService.getAllComplaints();
    }

    @GetMapping("/{id}")
    public Complaint getComplaintById(@PathVariable Long id) {
        return complaintService.getComplaintById(id);
    }
}
