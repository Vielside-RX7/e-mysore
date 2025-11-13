package com.emysore.ecom_mysore_backend.controller;

import com.emysore.ecom_mysore_backend.model.Notification;
import com.emysore.ecom_mysore_backend.model.User;
import com.emysore.ecom_mysore_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<Notification>> getUserNotifications(Authentication authentication, Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUserNotifications(user, pageable));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(user)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
}