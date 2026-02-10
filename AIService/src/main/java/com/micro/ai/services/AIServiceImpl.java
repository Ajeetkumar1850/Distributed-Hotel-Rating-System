package com.micro.ai.services;

import com.micro.ai.entities.*;
import com.micro.ai.external.services.HotelService;
import com.micro.ai.external.services.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private HotelService hotelService;

    @Override
    public ReviewSummary summarizeReviews(String hotelId) {
        logger.info("Fetching reviews for hotel: {}", hotelId);

        // 1. Fetch hotel details
        Hotel hotel = hotelService.getHotel(hotelId);
        
        // 2. Fetch all reviews for this hotel
        List<Rating> ratings = ratingService.getRatingsByHotel(hotelId);

        if (ratings.isEmpty()) {
            return createEmptySummary(hotel);
        }

        // 3. Calculate statistics
        double avgRating = ratings.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0.0);

        // 4. Prepare reviews text for AI
        StringBuilder reviewsText = new StringBuilder();
        for (Rating rating : ratings) {
            reviewsText.append(String.format(
                "Rating: %d/5, Feedback: %s\n", 
                rating.getRating(), 
                rating.getFeedback()
            ));
        }

        // 5. Create AI prompt
        String promptText = """
            Analyze these hotel reviews and provide:
            1. A brief summary (2-3 sentences)
            2. Top 3 pros (bullet points)
            3. Top 3 cons (bullet points)
            4. Overall sentiment (positive/negative/neutral)
            
            Hotel: {hotelName}
            Location: {location}
            Average Rating: {avgRating}/5
            Total Reviews: {totalReviews}
            
            Reviews:
            {reviews}
            
            Format your response as:
            SUMMARY: [your summary]
            PROS: [bullet points]
            CONS: [bullet points]
            SENTIMENT: [positive/negative/neutral]
            """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of(
            "hotelName", hotel.getName(),
            "location", hotel.getLocation(),
            "avgRating", String.format("%.1f", avgRating),
            "totalReviews", String.valueOf(ratings.size()),
            "reviews", reviewsText.toString()
        ));

        // 6. Call Gemini API
        logger.info("Calling Gemini API for review summarization");
        String aiResponse = chatClient.prompt(prompt).call().content();
        logger.info("AI Response received");

        // 7. Parse AI response
        return parseAIResponse(aiResponse, hotel, ratings.size(), avgRating);
    }

    private ReviewSummary createEmptySummary(Hotel hotel) {
        ReviewSummary summary = new ReviewSummary();
        summary.setHotelId(hotel.getHotelId());
        summary.setHotelName(hotel.getName());
        summary.setTotalReviews(0);
        summary.setAverageRating(0.0);
        summary.setSummary("No reviews available yet.");
        summary.setPros("N/A");
        summary.setCons("N/A");
        summary.setSentiment("neutral");
        summary.setGeneratedAt(LocalDateTime.now().toString());
        return summary;
    }

    private ReviewSummary parseAIResponse(String aiResponse, Hotel hotel, 
                                         int totalReviews, double avgRating) {
        ReviewSummary summary = new ReviewSummary();
        summary.setHotelId(hotel.getHotelId());
        summary.setHotelName(hotel.getName());
        summary.setTotalReviews(totalReviews);
        summary.setAverageRating(avgRating);

        // Parse AI response
        String[] sections = aiResponse.split("SUMMARY:|PROS:|CONS:|SENTIMENT:");
        
        if (sections.length >= 2) {
            summary.setSummary(sections[1].trim().split("PROS:")[0].trim());
        }
        if (sections.length >= 3) {
            summary.setPros(sections[2].trim().split("CONS:")[0].trim());
        }
        if (sections.length >= 4) {
            summary.setCons(sections[3].trim().split("SENTIMENT:")[0].trim());
        }
        if (sections.length >= 5) {
            summary.setSentiment(sections[4].trim().toLowerCase());
        }

        summary.setGeneratedAt(LocalDateTime.now().toString());
        return summary;
    }
}

    @Override
    public HotelRecommendation recommendHotels(String query) {
        logger.info("Processing hotel recommendation request: {}", query);

        // 1. Fetch all hotels
        List<Hotel> allHotels = hotelService.getAllHotels();
        
        if (allHotels.isEmpty()) {
            return createEmptyRecommendation(query);
        }

        // 2. Fetch ratings for each hotel to calculate average
        List<RecommendedHotel> hotelsWithRatings = new ArrayList<>();
        
        for (Hotel hotel : allHotels) {
            try {
                List<Rating> ratings = ratingService.getRatingsByHotel(hotel.getHotelId());
                
                double avgRating = ratings.isEmpty() ? 0.0 : 
                    ratings.stream()
                        .mapToInt(Rating::getRating)
                        .average()
                        .orElse(0.0);
                
                RecommendedHotel recHotel = new RecommendedHotel();
                recHotel.setHotelId(hotel.getHotelId());
                recHotel.setName(hotel.getName());
                recHotel.setLocation(hotel.getLocation());
                recHotel.setAbout(hotel.getAbout());
                recHotel.setAverageRating(avgRating);
                recHotel.setTotalReviews(ratings.size());
                
                hotelsWithRatings.add(recHotel);
            } catch (Exception e) {
                logger.warn("Could not fetch ratings for hotel: {}", hotel.getHotelId());
            }
        }

        // 3. Prepare hotel data for AI
        StringBuilder hotelsData = new StringBuilder();
        for (RecommendedHotel hotel : hotelsWithRatings) {
            hotelsData.append(String.format(
                "Hotel ID: %s, Name: %s, Location: %s, About: %s, Rating: %.1f/5, Reviews: %d\n",
                hotel.getHotelId(),
                hotel.getName(),
                hotel.getLocation(),
                hotel.getAbout(),
                hotel.getAverageRating(),
                hotel.getTotalReviews()
            ));
        }

        // 4. Create AI prompt for recommendations
        String promptText = """
            Based on the user's query, recommend the top 3 most suitable hotels from the list below.
            
            User Query: {query}
            
            Available Hotels:
            {hotels}
            
            Provide your response in this exact format:
            HOTEL_1: [Hotel ID]
            REASON_1: [Why this hotel matches the query]
            HOTEL_2: [Hotel ID]
            REASON_2: [Why this hotel matches the query]
            HOTEL_3: [Hotel ID]
            REASON_3: [Why this hotel matches the query]
            EXPLANATION: [Overall explanation of recommendations]
            """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Prompt prompt = promptTemplate.create(Map.of(
            "query", query,
            "hotels", hotelsData.toString()
        ));

        // 5. Call Gemini API
        logger.info("Calling Gemini API for hotel recommendations");
        String aiResponse = chatClient.prompt(prompt).call().content();
        logger.info("AI recommendations received");

        // 6. Parse AI response and build recommendation
        return parseRecommendationResponse(aiResponse, hotelsWithRatings, query);
    }

    private HotelRecommendation createEmptyRecommendation(String query) {
        HotelRecommendation recommendation = new HotelRecommendation();
        recommendation.setQuery(query);
        recommendation.setRecommendations(new ArrayList<>());
        recommendation.setExplanation("No hotels available for recommendation.");
        return recommendation;
    }

    private HotelRecommendation parseRecommendationResponse(String aiResponse, 
                                                           List<RecommendedHotel> allHotels,
                                                           String query) {
        HotelRecommendation recommendation = new HotelRecommendation();
        recommendation.setQuery(query);
        
        List<RecommendedHotel> recommendations = new ArrayList<>();
        
        // Parse AI response
        String[] lines = aiResponse.split("\n");
        String currentHotelId = null;
        String currentReason = null;
        String explanation = "";
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.startsWith("HOTEL_")) {
                if (currentHotelId != null && currentReason != null) {
                    addRecommendation(recommendations, allHotels, currentHotelId, currentReason);
                }
                currentHotelId = line.split(":", 2)[1].trim();
                currentReason = null;
            } else if (line.startsWith("REASON_")) {
                currentReason = line.split(":", 2)[1].trim();
                if (currentHotelId != null) {
                    addRecommendation(recommendations, allHotels, currentHotelId, currentReason);
                    currentHotelId = null;
                    currentReason = null;
                }
            } else if (line.startsWith("EXPLANATION:")) {
                explanation = line.split(":", 2)[1].trim();
            }
        }
        
        recommendation.setRecommendations(recommendations);
        recommendation.setExplanation(explanation.isEmpty() ? 
            "AI-powered recommendations based on your query." : explanation);
        
        return recommendation;
    }

    private void addRecommendation(List<RecommendedHotel> recommendations,
                                  List<RecommendedHotel> allHotels,
                                  String hotelId,
                                  String reason) {
        for (RecommendedHotel hotel : allHotels) {
            if (hotel.getHotelId().equals(hotelId)) {
                RecommendedHotel rec = new RecommendedHotel();
                rec.setHotelId(hotel.getHotelId());
                rec.setName(hotel.getName());
                rec.setLocation(hotel.getLocation());
                rec.setAbout(hotel.getAbout());
                rec.setAverageRating(hotel.getAverageRating());
                rec.setTotalReviews(hotel.getTotalReviews());
                rec.setReason(reason);
                recommendations.add(rec);
                break;
            }
        }
    }
