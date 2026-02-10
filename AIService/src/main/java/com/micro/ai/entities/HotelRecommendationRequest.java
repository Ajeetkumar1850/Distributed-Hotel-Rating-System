package com.micro.ai.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelRecommendationRequest {
    private String query;  // Natural language query like "luxury hotel near beach"
}
