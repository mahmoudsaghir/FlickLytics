package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Review;
import models.ReviewsSummary;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ReviewsService class.
 * Tests review fetching and sentiment analysis integration.
 *
 * Testing strategy:
 * - Mock TMDb API responses
 * - Test review parsing and sentiment analysis
 * - Test handling of API responses with various review counts
 * - Verify proper aggregation of sentiment data
 *
 * @author Tasmia Naomi
 */
public class ReviewsServiceTest {

    private ReviewsService reviewsService;
    private TmdbService tmdbService;
    private ObjectMapper objectMapper;

    /**
     * Sets up test fixtures before each test.
     * Creates mock TmdbService and ReviewsService instances.
     *
     * @author Tasmia Naomi
     */
    @Before
    public void setUp() {
        tmdbService = mock(TmdbService.class);
        reviewsService = new ReviewsService(tmdbService);
        objectMapper = new ObjectMapper();
    }

    /**
     * Tests getReviewsWithSentiment with no reviews in API response.
     * Should return a ReviewsSummary with 0 reviews.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsWithNoReviews() throws Exception {
        String reviewsJson = "{\"results\": []}";
        JsonNode reviewsRoot = objectMapper.readTree(reviewsJson);

        when(tmdbService.getReviews(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(reviewsRoot);

        ReviewsSummary summary = reviewsService.getReviewsWithSentiment(
                "http://api.tmdb.org", "token", "movie", 123L);

        assertNotNull(summary);
        assertEquals(0, summary.getTotalReviews());
        assertEquals(":-|", summary.getGlobalSentiment());
    }

    /**
     * Tests getReviewsWithSentiment with happy reviews.
     * Should correctly classify sentiments.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsWithHappyReviews() throws Exception {
        String reviewsJson = """
                {
                    "results": [
                        {
                            "author": "John",
                            "content": "This movie is amazing and wonderful! Absolutely loved it!"
                        },
                        {
                            "author": "Jane",
                            "content": "Excellent film! I was impressed by the brilliant acting and fantastic story."
                        }
                    ]
                }
                """;

        JsonNode reviewsRoot = objectMapper.readTree(reviewsJson);

        when(tmdbService.getReviews(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(reviewsRoot);

        ReviewsSummary summary = reviewsService.getReviewsWithSentiment(
                "http://api.tmdb.org", "token", "movie", 123L);

        assertNotNull(summary);
        assertEquals(2, summary.getTotalReviews());
        assertEquals(2, summary.getHappyCount());
        assertEquals(0, summary.getSadCount());
        assertEquals(":-)", summary.getGlobalSentiment());
    }

    /**
     * Tests getReviewsWithSentiment with sad reviews.
     * Should correctly classify sentiments.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsWithSadReviews() throws Exception {
        String reviewsJson = """
                {
                    "results": [
                        {
                            "author": "John",
                            "content": "This movie is terrible and awful! I hated every second of it."
                        },
                        {
                            "author": "Jane",
                            "content": "Horrible film. Boring and dreadful. Worst experience ever!"
                        }
                    ]
                }
                """;

        JsonNode reviewsRoot = objectMapper.readTree(reviewsJson);

        when(tmdbService.getReviews(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(reviewsRoot);

        ReviewsSummary summary = reviewsService.getReviewsWithSentiment(
                "http://api.tmdb.org", "token", "movie", 123L);

        assertNotNull(summary);
        assertEquals(2, summary.getTotalReviews());
        assertEquals(0, summary.getHappyCount());
        assertEquals(2, summary.getSadCount());
        assertEquals(":-(", summary.getGlobalSentiment());
    }

    /**
     * Tests getReviewsWithSentiment with mixed reviews.
     * Should correctly classify and aggregate sentiments.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsWithMixedReviews() throws Exception {
        String reviewsJson = """
                {
                    "results": [
                        {
                            "author": "John",
                            "content": "Good parts and bad parts. Mixed feelings overall."
                        },
                        {
                            "author": "Jane",
                            "content": "The movie has some great moments but also boring sections."
                        }
                    ]
                }
                """;

        JsonNode reviewsRoot = objectMapper.readTree(reviewsJson);

        when(tmdbService.getReviews(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(reviewsRoot);

        ReviewsSummary summary = reviewsService.getReviewsWithSentiment(
                "http://api.tmdb.org", "token", "movie", 123L);

        assertNotNull(summary);
        assertEquals(2, summary.getTotalReviews());
        assertTrue(summary.getHappyCount() >= 0);
        assertTrue(summary.getSadCount() >= 0);
    }

    /**
     * Tests getReviewsWithSentiment limit of 50 reviews.
     * Should only process first 50 reviews even if API returns more.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsLimitedTo50() throws Exception {
        StringBuilder jsonBuilder = new StringBuilder("{\"results\": [");
        for (int i = 0; i < 60; i++) {
            jsonBuilder.append("{\"author\": \"Author").append(i)
                    .append("\", \"content\": \"Great movie!\"}");
            if (i < 59) jsonBuilder.append(",");
        }
        jsonBuilder.append("]}");

        JsonNode reviewsRoot = objectMapper.readTree(jsonBuilder.toString());

        when(tmdbService.getReviews(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(reviewsRoot);

        ReviewsSummary summary = reviewsService.getReviewsWithSentiment(
                "http://api.tmdb.org", "token", "movie", 123L);

        assertNotNull(summary);
        // Should be limited to 50
        assertEquals(50, summary.getTotalReviews());
    }

    /**
     * Tests that individual Review objects are correctly populated in ReviewsSummary.
     * Verifies sentiment and percentage values are calculated correctly.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testReviewDataIntegrity() throws Exception {
        String reviewsJson = """
                {
                    "results": [
                        {
                            "author": "TestAuthor",
                            "content": "This is excellent and wonderful!"
                        }
                    ]
                }
                """;

        JsonNode reviewsRoot = objectMapper.readTree(reviewsJson);

        when(tmdbService.getReviews(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(reviewsRoot);

        ReviewsSummary summary = reviewsService.getReviewsWithSentiment(
                "http://api.tmdb.org", "token", "movie", 123L);

        assertNotNull(summary);
        assertEquals(1, summary.getTotalReviews());

        Review review = summary.getReviews().get(0);
        assertEquals("TestAuthor", review.getAuthor());
        assertEquals("This is excellent and wonderful!", review.getContent());
        assertEquals(":-)", review.getSentiment());
        assertTrue(review.getHappyPercentage() > 0);
        assertEquals(0.0, review.getSadPercentage(), 0.01);
    }

    /**
     * Tests getReviewsWithSentiment with empty review content.
     * Should handle gracefully without crashing.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsWithEmptyContent() throws Exception {
        String reviewsJson = """
                {
                    "results": [
                        {
                            "author": "John",
                            "content": ""
                        }
                    ]
                }
                """;

        JsonNode reviewsRoot = objectMapper.readTree(reviewsJson);

        when(tmdbService.getReviews(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(reviewsRoot);

        ReviewsSummary summary = reviewsService.getReviewsWithSentiment(
                "http://api.tmdb.org", "token", "movie", 123L);

        assertNotNull(summary);
        assertEquals(1, summary.getTotalReviews());
        assertEquals(":-|", summary.getReviews().get(0).getSentiment());
    }
}


