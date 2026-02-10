package com.micro.ai.controllers;
import com.micro.ai.entities.HotelRecommendation;
import com.micro.ai.entities.HotelRecommendationRequest;
import com.micro.ai.entities.ReviewSummary;
import com.micro.ai.services.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/micro/ai")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Autowired
    private AIService aiService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("Health check endpoint called");
        return ResponseEntity.ok("AI Service is running successfully! 🚀");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        logger.info("Test endpoint called");
        return ResponseEntity.ok("AI Service test endpoint working!");
    }

    @GetMapping("/hotels/{hotelId}/review-summary")
    public ResponseEntity<ReviewSummary> getReviewSummary(@PathVariable String hotelId) {
        logger.info("Request received for review summary of hotel: {}", hotelId);
        
        try {
            ReviewSummary summary = aiService.summarizeReviews(hotelId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error generating review summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/recommend")
    public ResponseEntity<HotelRecommendation> recommendHotels(
            @RequestBody HotelRecommendationRequest request) {
        logger.info("Request received for hotel recommendations: {}", request.getQuery());
        
        try {
            HotelRecommendation recommendations = aiService.recommendHotels(request.getQuery());
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error generating recommendations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
