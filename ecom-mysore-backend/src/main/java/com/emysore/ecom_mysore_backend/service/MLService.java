package com.emysore.ecom_mysore_backend.service;

import com.emysore.ecom_mysore_backend.model.Complaint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class MLService {
    private static final Logger logger = LoggerFactory.getLogger(MLService.class);

    @Value("${ml.service.url:http://localhost:8000}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate;

    public MLService() {
        this.restTemplate = new RestTemplate();
    }

    public void enrichComplaint(Complaint complaint) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("title", complaint.getTitle());
            request.put("description", complaint.getDescription());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String predictUrl = mlServiceUrl + "/predict";
            Map<String, Object> response = restTemplate.postForObject(predictUrl, entity, Map.class);

            if (response != null) {
                updateComplaintFromMLResponse(complaint, response);
                logger.info("Successfully enriched complaint #{} with ML analysis", complaint.getId());
            }
        } catch (Exception e) {
            logger.error("Failed to enrich complaint with ML analysis", e);
            setDefaultMLValues(complaint);
        }
    }

    private void updateComplaintFromMLResponse(Complaint complaint, Map<String, Object> response) {
        try {
            Object category = response.get("category");
            Object urgency = response.get("urgency");
            Object sentiment = response.get("sentiment");
            Object confidence = response.get("confidence");

            if ((complaint.getCategory() == null || complaint.getCategory().isEmpty()) && category != null) {
                complaint.setCategory(category.toString());
            }
            
            if (urgency != null) {
                complaint.setUrgency(urgency.toString());
            }
            
            if (sentiment != null) {
                complaint.setSentiment(sentiment.toString());
            }
            
            if (confidence != null) {
                try {
                    complaint.setConfidenceScore(Double.valueOf(confidence.toString()));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid confidence score format in ML response", e);
                    complaint.setConfidenceScore(0.5); // Default confidence
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing ML response", e);
            setDefaultMLValues(complaint);
        }
    }

    private void setDefaultMLValues(Complaint complaint) {
        complaint.setUrgency("MEDIUM");
        complaint.setSentiment("NEUTRAL");
        if (complaint.getCategory() == null || complaint.getCategory().isEmpty()) {
            complaint.setCategory("GENERAL");
        }
        complaint.setConfidenceScore(0.5);
    }

    public Map<String, Object> analyzeSentiment(String text) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String sentimentUrl = mlServiceUrl + "/sentiment";
            return restTemplate.postForObject(sentimentUrl, entity, Map.class);
        } catch (Exception e) {
            logger.error("Failed to analyze sentiment", e);
            return Map.of(
                "sentiment", "NEUTRAL",
                "confidence", 0.5
            );
        }
    }

    public Map<String, Object> predictCategory(String text) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            String categoryUrl = mlServiceUrl + "/category";
            return restTemplate.postForObject(categoryUrl, entity, Map.class);
        } catch (Exception e) {
            logger.error("Failed to predict category", e);
            return Map.of(
                "category", "GENERAL",
                "confidence", 0.5
            );
        }
    }
}
