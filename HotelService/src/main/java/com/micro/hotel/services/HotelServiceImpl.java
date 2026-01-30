package com.micro.hotel.services;

import com.micro.hotel.exceptions.HotelException;
import com.micro.hotel.entities.Hotel;
import com.micro.hotel.repositories.HotelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HotelServiceImpl implements HotelService{

    @Autowired
    private HotelRepository hotelRepository;
    
    private Logger logger = LoggerFactory.getLogger(HotelServiceImpl.class);

    @Override
    @CacheEvict(value = {"hotels", "hotel"}, allEntries = true)
    public Hotel createHotel(Hotel hotel) {
        logger.info("Creating new hotel, clearing cache");
        hotel.setHotelId(UUID.randomUUID().toString());
        return hotelRepository.save(hotel);
    }

    @Override
    @Cacheable(value = "hotels", unless = "#result == null || #result.isEmpty()")
    public List<Hotel> getAllHotels() {
        logger.info("Fetching all hotels from database (cache miss)");
        return hotelRepository.findAll();
    }

    @Override
    @Cacheable(value = "hotel", key = "#hotelId", unless = "#result == null")
    public Hotel getHotel(String hotelId) {
        logger.info("Fetching hotel {} from database (cache miss)", hotelId);
        return hotelRepository.findById(hotelId).orElseThrow(() -> new HotelException("Hotel not found with hotelId: " + hotelId));
    }
}
