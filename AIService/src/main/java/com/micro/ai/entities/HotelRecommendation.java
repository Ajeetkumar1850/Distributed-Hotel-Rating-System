package com.micro.ai.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelRecommendation {
    private List<RecommendedHotel> recommendations;
    private String explanation;
    private String query;
}
