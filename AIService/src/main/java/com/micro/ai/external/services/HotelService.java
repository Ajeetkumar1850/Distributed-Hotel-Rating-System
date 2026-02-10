package com.micro.ai.external.services;

import com.micro.ai.entities.Hotel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "HOTEL-SERVICE")
public interface HotelService {
    
    @GetMapping("/micro/hotels/{hotelId}")
    Hotel getHotel(@PathVariable("hotelId") String hotelId);
    
    @GetMapping("/micro/hotels/")
    List<Hotel> getAllHotels();
}
