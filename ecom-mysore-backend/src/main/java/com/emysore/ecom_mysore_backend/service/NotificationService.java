package com.emysore.ecom_mysore_backend.service;

import com.emysore.ecom_mysore_backend.model.Notification;
import com.emysore.ecom_mysore_backend.model.User;
import com.emysore.ecom_mysore_backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(User user, String title, String message, Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        
        notification = notificationRepository.save(notification);
        
        // Async notification dispatch
        sendNotificationAsync(notification);
        
        return notification;
    }

    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;

    @Async
    protected void sendNotificationAsync(Notification notification) {
        User user = notification.getUser();
        String title = notification.getTitle();
        String message = notification.getMessage();

        // Send email if user has email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            try {
                emailService.sendEmail(user.getEmail(), title, message);
            } catch (Exception e) {
                logger.error("Failed to send email notification", e);
            }
        }

        // Send SMS if user has phone
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            try {
                smsService.sendSMS(user.getPhone(), title + ": " + message);
            } catch (Exception e) {
                logger.error("Failed to send SMS notification", e);
            }
        }
    }

    @Transactional(readOnly = true)
    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadOrderByCreatedAtDesc(user, false);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndRead(user, false);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadOrderByCreatedAtDesc(user, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Send a direct notification to an external contact (department email/phone) without
     * persisting a Notification entity.
     */
    public void sendDirectContactNotification(String email, String phone, String title, String message) {
        if (email != null && !email.isEmpty()) {
            try {
                emailService.sendEmail(email, title, message);
            } catch (Exception e) {
                logger.error("Failed to send direct email to {}", email, e);
            }
        }

        if (phone != null && !phone.isEmpty()) {
            try {
                smsService.sendSMS(phone, title + ": " + message);
            } catch (Exception e) {
                logger.error("Failed to send direct SMS to {}", phone, e);
            }
        }
    }
}