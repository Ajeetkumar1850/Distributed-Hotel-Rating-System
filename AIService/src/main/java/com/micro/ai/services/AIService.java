package com.micro.ai.services;

import com.micro.ai.entities.HotelRecommendation;
import com.micro.ai.entities.ReviewSummary;

public interface AIService {
    ReviewSummary summarizeReviews(String hotelId);
    HotelRecommendation recommendHotels(String query);
}
