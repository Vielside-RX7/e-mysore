package com.emysore.ecom_mysore_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EscalationScheduler {

    @Autowired
    private ComplaintService complaintService;

    // Runs every 5 minutes in dev; adjust for production
    @Scheduled(fixedDelayString = "PT5M")
    public void runEscalation() {
        try {
            complaintService.escalateOverdue();
        } catch (Exception e) {
            // log and continue
            System.err.println("Escalation job failed: " + e.getMessage());
        }
    }
}
