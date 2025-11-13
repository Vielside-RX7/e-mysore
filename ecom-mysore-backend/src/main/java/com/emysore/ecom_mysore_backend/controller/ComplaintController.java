package com.emysore.ecom_mysore_backend.controller;

import com.emysore.ecom_mysore_backend.model.*;
import com.emysore.ecom_mysore_backend.service.*;
import com.emysore.ecom_mysore_backend.repository.UserRepository;
import com.emysore.ecom_mysore_backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<?> createComplaint(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
        try {
            User user = ((UserPrincipal) authentication.getPrincipal()).getUser();
            
            Complaint complaint = new Complaint();
            complaint.setTitle(title);
            complaint.setDescription(description);
            complaint.setCategory(category);
            complaint.setLocation(location);

            return ResponseEntity.ok(complaintService.createComplaint(complaint, user, image));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/with-image", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<?> createComplaintWithImage(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam("location") String location,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
        try {
            User user = ((UserPrincipal) authentication.getPrincipal()).getUser();
            
            Complaint complaint = new Complaint();
            complaint.setTitle(title);
            complaint.setDescription(description);
            complaint.setCategory(category);
            complaint.setLocation(location);

            return ResponseEntity.ok(complaintService.createComplaint(complaint, user, image));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<Complaint>> getComplaints(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "escalated", required = false) Boolean escalated,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String[] sort) {
        
        // parse sort param as property and direction (e.g. createdAt,desc)
        String sortProp = sort.length > 0 ? sort[0] : "createdAt";
        String sortDir = sort.length > 1 ? sort[1] : "desc";
        PageRequest pageRequest = PageRequest.of(page, size,
            Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortProp));
        
        return ResponseEntity.ok(complaintService.getComplaints(status, category, escalated, pageRequest));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Complaint>> searchComplaints(
            @RequestParam("q") String searchTerm) {
        return ResponseEntity.ok(complaintService.searchComplaints(searchTerm));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User officer = ((UserPrincipal) authentication.getPrincipal()).getUser();
            String newStatus = body.get("status");
            String remarks = body.get("remarks");

            Complaint updated = complaintService.updateStatus(id, newStatus, officer, remarks);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public ResponseEntity<?> escalateComplaint(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        try {
            User officer = ((UserPrincipal) authentication.getPrincipal()).getUser();
            Boolean escalate = (Boolean) body.getOrDefault("escalated", true);
            
            Complaint updated = complaintService.escalateComplaint(id, escalate, officer);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            User user = ((UserPrincipal) authentication.getPrincipal()).getUser();
            String comment = body.get("comment");
            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Comment cannot be empty"));
            }

            complaintService.addComment(id, user, comment);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<ComplaintAuditLog>> getAuditLogs(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintAuditLogs(id));
    }
}
