package com.micro.ai.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSummary {
    private String hotelId;
    private String hotelName;
    private int totalReviews;
    private double averageRating;
    private String summary;          // AI-generated summary
    private String pros;             // AI-generated pros
    private String cons;             // AI-generated cons
    private String sentiment;        // positive/negative/neutral
    private String generatedAt;      // timestamp
}
