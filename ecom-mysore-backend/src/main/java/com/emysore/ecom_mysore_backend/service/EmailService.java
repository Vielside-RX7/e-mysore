package com.emysore.ecom_mysore_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
            
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to: {}", to, e);
            // In production, you might want to handle this differently
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendVerificationEmail(String to, String verificationLink) {
        String subject = "E-Mysore - Verify Your Email";
        String text = "Welcome to E-Mysore!\n\n" +
                     "Please click on the link below to verify your email address:\n" +
                     verificationLink + "\n\n" +
                     "If you didn't request this verification, please ignore this email.\n\n" +
                     "Best regards,\n" +
                     "E-Mysore Team";
        
        sendEmail(to, subject, text);
    }

    public void sendComplaintStatusUpdate(String to, String complaintId, String status) {
        String subject = "Complaint #" + complaintId + " Status Update";
        String text = "Dear Citizen,\n\n" +
                     "Your complaint (ID: " + complaintId + ") status has been updated to: " + status + "\n\n" +
                     "You can check the details by logging into your E-Mysore account.\n\n" +
                     "Best regards,\n" +
                     "E-Mysore Team";
        
        sendEmail(to, subject, text);
    }

    public void sendEscalationNotification(String to, String complaintId) {
        String subject = "Complaint #" + complaintId + " Escalated";
        String text = "Dear Citizen,\n\n" +
                     "Your complaint (ID: " + complaintId + ") has been escalated due to delay in resolution.\n\n" +
                     "A higher authority has been notified and will look into your complaint.\n\n" +
                     "We apologize for the delay and appreciate your patience.\n\n" +
                     "Best regards,\n" +
                     "E-Mysore Team";
        
        sendEmail(to, subject, text);
    }
}