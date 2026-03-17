package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Review;
import models.ReviewSentimentAnalyzer;
import models.ReviewsSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service responsible for fetching reviews from the TMDb API and performing sentiment analysis.
 * Retrieves up to 50 reviews and analyzes the sentiment of each review.
 *
 * @author Tasmia Naomi
 */
public class ReviewsService {

    private final TmdbService tmdbService;

    /**
     * Constructs the ReviewsService with a TmdbService dependency.
     *
     * @param tmdbService The service for TMDb API communications
     * @author Tasmia Naomi
     */
    @Inject
    public ReviewsService(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    /**
     * Fetches reviews for a given media item and performs sentiment analysis on each review.
     * Returns a ReviewsSummary containing up to 50 reviews with their sentiments.
     *
     * @param apiUrl The TMDb API base URL
     * @param token Bearer token for authorization
     * @param type The media type ("movie" or "tv")
     * @param id The TMDb media ID
     * @return A ReviewsSummary containing analyzed reviews and aggregated sentiment data
     * @throws java.lang.Exception if the API request fails
     * @author Tasmia Naomi
     */
    public ReviewsSummary getReviewsWithSentiment(String apiUrl, String token, String type, Long id) throws Exception {
        JsonNode reviewsRoot = tmdbService.getReviews(apiUrl, token, type, id);

        List<Review> reviews = StreamSupport.stream(
                reviewsRoot.path("results").spliterator(), false)
                .limit(50)
                .map(reviewNode -> {
                    String author = reviewNode.path("author").asText("Anonymous");
                    String content = reviewNode.path("content").asText("");
                    String sentiment = ReviewSentimentAnalyzer.analyzeSentiment(content);
                    double happyPercentage = ReviewSentimentAnalyzer.getHappyPercentage(content);
                    double sadPercentage = ReviewSentimentAnalyzer.getSadPercentage(content);

                    return new Review(author, content, sentiment, happyPercentage, sadPercentage);
                })
                .collect(Collectors.toList());

        return new ReviewsSummary(reviews);
    }
}



