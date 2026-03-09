package models;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Unit tests for the ReviewsSummary class.
 * Tests sentiment aggregation and statistical calculations.
 *
 * Testing strategy:
 * - Empty review list handling
 * - Happy sentiment aggregation (>70% happy reviews)
 * - Sad sentiment aggregation (>70% sad reviews)
 * - Neutral sentiment aggregation
 * - Most common sentiment determination
 * - Sentiment count tracking
 *
 * @author Tasmia Naomi
 */
public class ReviewsSummaryTest {

    /**
     * Tests ReviewsSummary with an empty review list.
     * All counts should be 0 and sentiment should be neutral.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testEmptyReviews() {
        List<Review> reviews = new ArrayList<>();
        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(0, summary.getTotalReviews());
        assertEquals(0, summary.getHappyCount());
        assertEquals(0, summary.getSadCount());
        assertEquals(0, summary.getNeutralCount());
        assertEquals(":-|", summary.getGlobalSentiment());
    }

    /**
     * Tests ReviewsSummary with all happy reviews.
     * Global sentiment should be happy, counts should reflect this.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAllHappyReviews() {
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reviews.add(new Review("Author" + i, "Great movie!", ":-)", 100.0, 0.0));
        }

        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(10, summary.getTotalReviews());
        assertEquals(10, summary.getHappyCount());
        assertEquals(0, summary.getSadCount());
        assertEquals(0, summary.getNeutralCount());
        assertEquals(":-)", summary.getGlobalSentiment());
        assertEquals(":-)", summary.getMostCommonSentiment());
    }

    /**
     * Tests ReviewsSummary with all sad reviews.
     * Global sentiment should be sad, counts should reflect this.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAllSadReviews() {
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reviews.add(new Review("Author" + i, "Terrible movie!", ":-(", 0.0, 100.0));
        }

        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(10, summary.getTotalReviews());
        assertEquals(0, summary.getHappyCount());
        assertEquals(10, summary.getSadCount());
        assertEquals(0, summary.getNeutralCount());
        assertEquals(":-(", summary.getGlobalSentiment());
        assertEquals(":-(", summary.getMostCommonSentiment());
    }

    /**
     * Tests ReviewsSummary with all neutral reviews.
     * Global sentiment should be neutral, counts should reflect this.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAllNeutralReviews() {
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            reviews.add(new Review("Author" + i, "Mixed feelings.", ":-|", 50.0, 50.0));
        }

        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(10, summary.getTotalReviews());
        assertEquals(0, summary.getHappyCount());
        assertEquals(0, summary.getSadCount());
        assertEquals(10, summary.getNeutralCount());
        assertEquals(":-|", summary.getGlobalSentiment());
        assertEquals(":-|", summary.getMostCommonSentiment());
    }

    /**
     * Tests ReviewsSummary with >70% happy reviews.
     * Global sentiment should be happy.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testMajorityHappyReviews() {
        List<Review> reviews = new ArrayList<>();
        // Add 8 happy reviews
        for (int i = 0; i < 8; i++) {
            reviews.add(new Review("Author" + i, "Great movie!", ":-)", 100.0, 0.0));
        }
        // Add 2 sad reviews
        for (int i = 8; i < 10; i++) {
            reviews.add(new Review("Author" + i, "Bad movie!", ":-(", 0.0, 100.0));
        }

        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(10, summary.getTotalReviews());
        assertEquals(8, summary.getHappyCount());
        assertEquals(2, summary.getSadCount());
        assertEquals(0, summary.getNeutralCount());
        // 80% happy > 70% threshold
        assertEquals(":-)", summary.getGlobalSentiment());
        assertEquals(":-)", summary.getMostCommonSentiment());
    }

    /**
     * Tests ReviewsSummary with >70% sad reviews.
     * Global sentiment should be sad.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testMajoritySadReviews() {
        List<Review> reviews = new ArrayList<>();
        // Add 8 sad reviews
        for (int i = 0; i < 8; i++) {
            reviews.add(new Review("Author" + i, "Bad movie!", ":-(", 0.0, 100.0));
        }
        // Add 2 happy reviews
        for (int i = 8; i < 10; i++) {
            reviews.add(new Review("Author" + i, "Great movie!", ":-)", 100.0, 0.0));
        }

        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(10, summary.getTotalReviews());
        assertEquals(2, summary.getHappyCount());
        assertEquals(8, summary.getSadCount());
        assertEquals(0, summary.getNeutralCount());
        // 80% sad > 70% threshold
        assertEquals(":-(", summary.getGlobalSentiment());
        assertEquals(":-(", summary.getMostCommonSentiment());
    }

    /**
     * Tests ReviewsSummary with mixed sentiments (no majority >70%).
     * Global sentiment should be neutral.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testMixedReviews() {
        List<Review> reviews = new ArrayList<>();
        // Add 4 happy reviews (40%)
        for (int i = 0; i < 4; i++) {
            reviews.add(new Review("Author" + i, "Good movie!", ":-)", 100.0, 0.0));
        }
        // Add 3 sad reviews (30%)
        for (int i = 4; i < 7; i++) {
            reviews.add(new Review("Author" + i, "Bad parts!", ":-(", 0.0, 100.0));
        }
        // Add 3 neutral reviews (30%)
        for (int i = 7; i < 10; i++) {
            reviews.add(new Review("Author" + i, "Mixed feelings.", ":-|", 50.0, 50.0));
        }

        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(10, summary.getTotalReviews());
        assertEquals(4, summary.getHappyCount());
        assertEquals(3, summary.getSadCount());
        assertEquals(3, summary.getNeutralCount());
        // No sentiment > 70%, so should be neutral
        assertEquals(":-|", summary.getGlobalSentiment());
    }

    /**
     * Tests single review in ReviewsSummary.
     * Global sentiment should match the single review's sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testSingleHappyReview() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("Author1", "Excellent film!", ":-)", 100.0, 0.0));

        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(1, summary.getTotalReviews());
        assertEquals(1, summary.getHappyCount());
        assertEquals(0, summary.getSadCount());
        assertEquals(0, summary.getNeutralCount());
        assertEquals(":-)", summary.getGlobalSentiment());
    }

    /**
     * Tests Review.getters are properly reflected in summary.
     * Verifies that the summary correctly aggregates review data.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testReviewDataIntegrity() {
        List<Review> reviews = new ArrayList<>();
        Review r1 = new Review("John", "Amazing film!", ":-)", 100.0, 0.0);
        Review r2 = new Review("Jane", "Terrible film!", ":-(", 0.0, 100.0);
        reviews.add(r1);
        reviews.add(r2);

        ReviewsSummary summary = new ReviewsSummary(reviews);

        assertEquals(2, summary.getReviews().size());
        assertEquals(r1, summary.getReviews().get(0));
        assertEquals(r2, summary.getReviews().get(1));
    }
}


