package com.micro.ai.external.services;

import com.micro.ai.entities.Rating;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "RATING-SERVICE")
public interface RatingService {
    @GetMapping("/micro/ratings/hotels/{hotelId}/")
    List<Rating> getRatingsByHotel(@PathVariable("hotelId") String hotelId);
}
