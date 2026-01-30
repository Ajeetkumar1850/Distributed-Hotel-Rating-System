package com.micro.user.services;

import com.micro.user.exceptions.UserException;
import com.micro.user.entities.Hotel;
import com.micro.user.entities.Rating;
import com.micro.user.external.services.HotelService;
import com.micro.user.repositories.UserRepository;
import com.micro.user.entities.User;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HotelService hotelService;

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @CacheEvict(value = {"users", "user"}, allEntries = true)
    public User saveUser(User user) {
        logger.info("Creating new user, clearing cache");
        Optional<User> emailOpt = userRepository.findByEmail(user.getEmail());
        Optional<User> phoneOpt = userRepository.findByPhone(user.getPhone());

        if(emailOpt.isPresent()) throw new UserException("User already present with Email: " + user.getEmail());
        if(phoneOpt.isPresent()) throw new UserException("User already present with Phone: " + user.getPhone());

        user.setUserId(UUID.randomUUID().toString());
        return userRepository.save(user);
    }

    @Override
    @Cacheable(value = "users", unless = "#result == null || #result.isEmpty()")
    public List<User> getAllUser() {
        logger.info("Fetching all users from database (cache miss)");
        List<User> users = userRepository.findAll();

        if(users.isEmpty()) throw new UserException("Users not found");

        return users;
    }

    @Override
    @CircuitBreaker(name = "ratingService", fallbackMethod = "getUserFallback")
    @Retry(name = "ratingService", fallbackMethod = "getUserFallback")
    @Cacheable(value = "user", key = "#userId", unless = "#result == null")
    public User getUser(String userId) {
        logger.info("Getting user with ID: {} (checking cache first)", userId);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException("User not found with userId: " + userId));

        // Get ratings from Rating Service with Circuit Breaker
        Rating[] userRatingsArray = getRatingsFromService(userId);

        if(userRatingsArray != null && userRatingsArray.length > 0) {
            List<Rating> userRatingsList = Arrays.stream(userRatingsArray).toList();
            List<Rating> userRatings = userRatingsList.stream().map(rating -> {
                // Get hotel details with Circuit Breaker
                Hotel hotel = getHotelWithCircuitBreaker(rating.getHotelId());
                rating.setHotel(hotel);
                return rating;
            }).toList();

            user.setRatings(userRatings);
        }
        else {
            user.setRatings(new ArrayList<>());
        }

        return user;
    }
    
    // Circuit Breaker for Rating Service
    @CircuitBreaker(name = "ratingService", fallbackMethod = "getRatingsFallback")
    private Rating[] getRatingsFromService(String userId) {
        logger.info("Calling Rating Service for user: {}", userId);
        return restTemplate.getForObject(
            "http://RATING-SERVICE/micro/ratings/users/" + userId + "/", 
            Rating[].class
        );
    }
    
    // Circuit Breaker for Hotel Service
    @CircuitBreaker(name = "hotelService", fallbackMethod = "getHotelFallback")
    private Hotel getHotelWithCircuitBreaker(String hotelId) {
        logger.info("Calling Hotel Service for hotel: {}", hotelId);
        return hotelService.getHotel(hotelId);
    }
    
    // Fallback method for getUser - Returns user without ratings
    public User getUserFallback(String userId, Exception ex) {
        logger.error("Fallback method called for getUser. Error: {}", ex.getMessage());
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException("User not found with userId: " + userId));
        
        user.setRatings(new ArrayList<>());
        logger.warn("Returning user without ratings due to service failure");
        
        return user;
    }
    
    // Fallback method for Rating Service - Returns empty array
    private Rating[] getRatingsFallback(String userId, Exception ex) {
        logger.error("Rating Service is down or slow. Fallback triggered. Error: {}", ex.getMessage());
        logger.warn("Returning empty ratings array for user: {}", userId);
        return new Rating[0];
    }
    
    // Fallback method for Hotel Service - Returns placeholder hotel
    private Hotel getHotelFallback(String hotelId, Exception ex) {
        logger.error("Hotel Service is down or slow. Fallback triggered. Error: {}", ex.getMessage());
        logger.warn("Returning placeholder hotel for hotelId: {}", hotelId);
        
        Hotel placeholderHotel = new Hotel();
        placeholderHotel.setHotelId(hotelId);
        placeholderHotel.setName("Service Unavailable");
        placeholderHotel.setLocation("N/A");
        placeholderHotel.setAbout("Hotel details temporarily unavailable");
        
        return placeholderHotel;
    }
}
