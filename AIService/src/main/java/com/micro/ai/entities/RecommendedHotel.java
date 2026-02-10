package com.micro.ai.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedHotel {
    private String hotelId;
    private String name;
    private String location;
    private String about;
    private double averageRating;
    private int totalReviews;
    private String reason;  // AI-generated reason for recommendation
}
