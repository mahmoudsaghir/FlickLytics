package models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Review model class.
 * Tests constructor initialization and all getter methods.
 *
 * Testing strategy:
 * - Normal review with all fields populated
 * - Happy, sad, neutral sentiments
 * - Boundary percentage values (0 and 100)
 * - Empty content and author
 *
 * @author Tasmia Naomi
 */
public class ReviewTest {

    /**
     * Tests getAuthor with a normal author name.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetAuthor() {
        Review review = new Review("John", "Great movie!", ":-)", 100.0, 0.0);
        assertEquals("John", review.getAuthor());
    }

    /**
     * Tests getContent with normal review text.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetContent() {
        Review review = new Review("Jane", "This movie was excellent!", ":-)", 100.0, 0.0);
        assertEquals("This movie was excellent!", review.getContent());
    }

    /**
     * Tests getSentiment with happy sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSentimentHappy() {
        Review review = new Review("A", "Amazing!", ":-)", 100.0, 0.0);
        assertEquals(":-)", review.getSentiment());
    }

    /**
     * Tests getSentiment with sad sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSentimentSad() {
        Review review = new Review("A", "Terrible!", ":-(", 0.0, 100.0);
        assertEquals(":-(", review.getSentiment());
    }

    /**
     * Tests getSentiment with neutral sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSentimentNeutral() {
        Review review = new Review("A", "Okay.", ":-|", 50.0, 50.0);
        assertEquals(":-|", review.getSentiment());
    }

    /**
     * Tests getHappyPercentage returns the correct value.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetHappyPercentage() {
        Review review = new Review("A", "Great!", ":-)", 85.5, 14.5);
        assertEquals(85.5, review.getHappyPercentage(), 0.01);
    }

    /**
     * Tests getSadPercentage returns the correct value.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSadPercentage() {
        Review review = new Review("A", "Bad!", ":-(", 14.5, 85.5);
        assertEquals(85.5, review.getSadPercentage(), 0.01);
    }

    /**
     * Tests Review with zero percentages.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testZeroPercentages() {
        Review review = new Review("A", "No sentiment.", ":-|", 0.0, 0.0);
        assertEquals(0.0, review.getHappyPercentage(), 0.01);
        assertEquals(0.0, review.getSadPercentage(), 0.01);
    }

    /**
     * Tests Review with 100% happy percentage boundary.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testFullHappyPercentage() {
        Review review = new Review("A", "All happy!", ":-)", 100.0, 0.0);
        assertEquals(100.0, review.getHappyPercentage(), 0.01);
        assertEquals(0.0, review.getSadPercentage(), 0.01);
    }

    /**
     * Tests Review with 100% sad percentage boundary.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testFullSadPercentage() {
        Review review = new Review("A", "All sad!", ":-(", 0.0, 100.0);
        assertEquals(0.0, review.getHappyPercentage(), 0.01);
        assertEquals(100.0, review.getSadPercentage(), 0.01);
    }

    /**
     * Tests Review with empty content string.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testEmptyContent() {
        Review review = new Review("A", "", ":-|", 0.0, 0.0);
        assertEquals("", review.getContent());
        assertEquals(":-|", review.getSentiment());
    }

    /**
     * Tests Review with empty author string.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testEmptyAuthor() {
        Review review = new Review("", "Content", ":-|", 50.0, 50.0);
        assertEquals("", review.getAuthor());
    }

    /**
     * Tests that all fields are correctly initialized together.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAllFieldsTogether() {
        Review review = new Review("TestUser", "A mixed review", ":-|", 45.0, 55.0);
        assertEquals("TestUser", review.getAuthor());
        assertEquals("A mixed review", review.getContent());
        assertEquals(":-|", review.getSentiment());
        assertEquals(45.0, review.getHappyPercentage(), 0.01);
        assertEquals(55.0, review.getSadPercentage(), 0.01);
    }
}
