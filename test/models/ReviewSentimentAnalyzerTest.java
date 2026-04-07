package models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the ReviewSentimentAnalyzer class.
 * Tests sentiment classification and happy/sad word percentage calculations.
 *
 * Testing strategy:
 * - Null and empty text handling
 * - Happy sentiment classification (>70% happy words)
 * - Sad sentiment classification (>70% sad words)
 * - Neutral sentiment classification
 * - Happy/sad percentage calculations
 * - Word boundary handling (punctuation removal)
 *
 * @author Tasmia Naomi
 */
public class ReviewSentimentAnalyzerTest {

    /**
     * Tests default constructor for coverage.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testConstructor() {
        ReviewSentimentAnalyzer analyzer = new ReviewSentimentAnalyzer();
        assertNotNull(analyzer);
    }

    /**
     * Tests sentiment analysis with null input.
     * Should return neutral sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAnalyzeSentimentWithNull() {
        String sentiment = ReviewSentimentAnalyzer.analyzeSentiment(null);
        assertEquals(":-|", sentiment);
    }

    /**
     * Tests sentiment analysis with empty string.
     * Should return neutral sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAnalyzeSentimentWithEmpty() {
        String sentiment = ReviewSentimentAnalyzer.analyzeSentiment("   ");
        assertEquals(":-|", sentiment);
    }

    /**
     * Tests sentiment analysis with strongly positive text (>70% happy words).
     * Should return happy sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAnalyzeSentimentWithHappyText() {
        String text = "This movie is amazing and wonderful! I absolutely loved it. It was fantastic and excellent!";
        String sentiment = ReviewSentimentAnalyzer.analyzeSentiment(text);
        assertEquals(":-)", sentiment);
    }

    /**
     * Tests sentiment analysis with strongly negative text (>70% sad words).
     * Should return sad sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAnalyzeSentimentWithSadText() {
        String text = "This was terrible and awful. I hated it. The movie was boring and dreadful. Worst film ever!";
        String sentiment = ReviewSentimentAnalyzer.analyzeSentiment(text);
        assertEquals(":-(", sentiment);
    }

    /**
     * Tests sentiment analysis with mixed positive and negative words.
     * Should return neutral sentiment (no sentiment dominates >70%).
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAnalyzeSentimentWithMixedText() {
        String text = "The movie was good but had some bad parts. I loved some scenes but hated others.";
        String sentiment = ReviewSentimentAnalyzer.analyzeSentiment(text);
        assertEquals(":-|", sentiment);
    }

    /**
     * Tests sentiment analysis with no sentiment words.
     * Should return neutral sentiment.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAnalyzeSentimentWithNoSentimentWords() {
        String text = "The movie has actors and a plot. It was released in 2024.";
        String sentiment = ReviewSentimentAnalyzer.analyzeSentiment(text);
        assertEquals(":-|", sentiment);
    }

    /**
     * Tests happy word percentage calculation with happy text.
     * Should return a value close to 100%.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetHappyPercentageWithHappyText() {
        String text = "This is excellent and wonderful!";
        double percentage = ReviewSentimentAnalyzer.getHappyPercentage(text);
        assertEquals(100.0, percentage, 0.01);
    }

    /**
     * Tests happy word percentage calculation with sad text.
     * Should return 0%.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetHappyPercentageWithSadText() {
        String text = "This is terrible and awful!";
        double percentage = ReviewSentimentAnalyzer.getHappyPercentage(text);
        assertEquals(0.0, percentage, 0.01);
    }

    /**
     * Tests sad word percentage calculation with sad text.
     * Should return a value close to 100%.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSadPercentageWithSadText() {
        String text = "This is terrible and awful!";
        double percentage = ReviewSentimentAnalyzer.getSadPercentage(text);
        assertEquals(100.0, percentage, 0.01);
    }

    /**
     * Tests sad word percentage calculation with happy text.
     * Should return 0%.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSadPercentageWithHappyText() {
        String text = "This is excellent and wonderful!";
        double percentage = ReviewSentimentAnalyzer.getSadPercentage(text);
        assertEquals(0.0, percentage, 0.01);
    }

    /**
     * Tests happy word percentage with null input.
     * Should return 0%.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetHappyPercentageWithNull() {
        double percentage = ReviewSentimentAnalyzer.getHappyPercentage(null);
        assertEquals(0.0, percentage, 0.01);
    }

    /**
     * Tests happy word percentage with empty string.
     * Should return 0%.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetHappyPercentageWithEmpty() {
        double percentage = ReviewSentimentAnalyzer.getHappyPercentage("   ");
        assertEquals(0.0, percentage, 0.01);
    }

    /**
     * Tests sad word percentage with null input.
     * Should return 0%.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSadPercentageWithNull() {
        double percentage = ReviewSentimentAnalyzer.getSadPercentage(null);
        assertEquals(0.0, percentage, 0.01);
    }

    /**
     * Tests sad word percentage with empty string.
     * Should return 0%.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetSadPercentageWithEmpty() {
        double percentage = ReviewSentimentAnalyzer.getSadPercentage("   ");
        assertEquals(0.0, percentage, 0.01);
    }

    /**
     * Tests sentiment analysis with text containing punctuation.
     * Should correctly identify words despite punctuation.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAnalyzeSentimentWithPunctuation() {
        String text = "Amazing! Wonderful? Excellent... Perfect!";
        String sentiment = ReviewSentimentAnalyzer.analyzeSentiment(text);
        assertEquals(":-)", sentiment);
    }

    /**
     * Tests sentiment analysis with text containing mixed case.
     * Should correctly analyze despite case variations.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testAnalyzeSentimentWithMixedCase() {
        String text = "This is AMAZING and WONDERFUL! Absolutely LOVE it!";
        String sentiment = ReviewSentimentAnalyzer.analyzeSentiment(text);
        assertEquals(":-)", sentiment);
    }

    /**
     * Tests countSentimentWords with null input.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testCountSentimentWordsWithNull() {
        int[] counts = ReviewSentimentAnalyzer.countSentimentWords(null);
        assertEquals(0, counts[0]);
        assertEquals(0, counts[1]);
    }

    /**
     * Tests countSentimentWords with empty string.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testCountSentimentWordsWithEmpty() {
        int[] counts = ReviewSentimentAnalyzer.countSentimentWords("   ");
        assertEquals(0, counts[0]);
        assertEquals(0, counts[1]);
    }

    /**
     * Tests countSentimentWords with happy words.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testCountSentimentWordsHappy() {
        int[] counts = ReviewSentimentAnalyzer.countSentimentWords("excellent wonderful");
        assertEquals(2, counts[0]);
        assertEquals(0, counts[1]);
    }

    /**
     * Tests countSentimentWords with sad words.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testCountSentimentWordsSad() {
        int[] counts = ReviewSentimentAnalyzer.countSentimentWords("terrible awful");
        assertEquals(0, counts[0]);
        assertEquals(2, counts[1]);
    }

    /**
     * Tests countSentimentWords with mixed words.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testCountSentimentWordsMixed() {
        int[] counts = ReviewSentimentAnalyzer.countSentimentWords("good bad excellent terrible");
        assertEquals(2, counts[0]);
        assertEquals(2, counts[1]);
    }

    /**
     * Tests countSentimentWords with no sentiment words.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testCountSentimentWordsNone() {
        int[] counts = ReviewSentimentAnalyzer.countSentimentWords("the movie has actors and a plot");
        assertEquals(0, counts[0]);
        assertEquals(0, counts[1]);
    }

    /**
     * Tests countSentimentWords with leading delimiters and repeated separators.
     * Ensures empty split tokens are skipped while valid words are counted.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testCountSentimentWordsSkipsEmptyTokens() {
        int[] counts = ReviewSentimentAnalyzer.countSentimentWords("!!!excellent---awful???");
        assertEquals(1, counts[0]);
        assertEquals(1, counts[1]);
    }
}



