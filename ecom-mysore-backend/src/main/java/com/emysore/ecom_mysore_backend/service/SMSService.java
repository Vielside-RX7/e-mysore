package com.emysore.ecom_mysore_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SMSService {
    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);

    @Value("${sms.api.key:dummy-key}")
    private String apiKey;

    @Value("${sms.api.url:http://localhost:8081/sms}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public SMSService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendSMS(String phoneNumber, String message) {
        try {
            // This is a stub implementation. In production, integrate with actual SMS gateway
            logger.info("Sending SMS to {} with message: {}", phoneNumber, message);
            
            // Simulate API call
            SMSRequest request = new SMSRequest(phoneNumber, message);
            // restTemplate.postForObject(apiUrl, request, SMSResponse.class);
            
            logger.info("SMS sent successfully to: {}", phoneNumber);
        } catch (Exception e) {
            logger.error("Failed to send SMS to: {}", phoneNumber, e);
            // In production, you might want to handle this differently
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    public void sendOTP(String phoneNumber, String otp) {
        String message = "Your E-Mysore verification code is: " + otp + 
                        ". Valid for 10 minutes. Do not share this code with anyone.";
        sendSMS(phoneNumber, message);
    }

    public void sendComplaintUpdate(String phoneNumber, String complaintId, String status) {
        String message = "E-Mysore: Your complaint #" + complaintId + 
                        " status has been updated to: " + status;
        sendSMS(phoneNumber, message);
    }

    public void sendEscalationAlert(String phoneNumber, String complaintId) {
        String message = "E-Mysore: Your complaint #" + complaintId + 
                        " has been escalated due to delay. A senior officer will review it.";
        sendSMS(phoneNumber, message);
    }

    // Inner class for SMS request payload
    private static class SMSRequest {
        private String to;
        private String message;

        public SMSRequest(String to, String message) {
            this.to = to;
            this.message = message;
        }

        // Getters
        public String getTo() { return to; }
        public String getMessage() { return message; }
    }
}