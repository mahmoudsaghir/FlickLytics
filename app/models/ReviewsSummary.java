package models;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates and summarizes sentiment analysis data across multiple reviews.
 * Provides global review sentiment, most common sentiment, and individual review details.
 *
 * @author Tasmia Naomi
 */
public class ReviewsSummary {
    private final String globalSentiment;
    private final String mostCommonSentiment;
    private final int happyCount;
    private final int sadCount;
    private final int neutralCount;
    private final List<Review> reviews;

    /**
     * Constructs a ReviewsSummary from a list of Review objects.
     * Automatically calculates global sentiment and sentiment distribution.
     *
     * @param reviews The list of Review objects with sentiment analysis
     * @author Tasmia Naomi
     */
    public ReviewsSummary(List<Review> reviews) {
        this.reviews = reviews;

        // Count sentiment distribution
        Map<String, Long> sentimentCounts = reviews.stream()
                .collect(Collectors.groupingBy(Review::getSentiment, Collectors.counting()));

        this.happyCount = sentimentCounts.getOrDefault(":-)", 0L).intValue();
        this.sadCount = sentimentCounts.getOrDefault(":-(", 0L).intValue();
        this.neutralCount = sentimentCounts.getOrDefault(":-|", 0L).intValue();

        // Determine most common sentiment
        this.mostCommonSentiment = sentimentCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(":-|");

        // Determine global sentiment
        this.globalSentiment = determineGlobalSentiment();
    }

    /**
     * Determines the global sentiment based on overall distribution.
     * If happy reviews > 70% of total, returns happy sentiment.
     * If sad reviews > 70% of total, returns sad sentiment.
     * Otherwise, returns neutral sentiment.
     *
     * @return The global sentiment emoticon
     * @author Tasmia Naomi
     */
    private String determineGlobalSentiment() {
        if (reviews.isEmpty()) {
            return ":-|";
        }

        int total = reviews.size();
        double happyPercentage = (double) happyCount / total * 100;
        double sadPercentage = (double) sadCount / total * 100;

        if (happyPercentage > 70) {
            return ":-)";
        } else if (sadPercentage > 70) {
            return ":-(";
        } else {
            return ":-|";
        }
    }

    /**
     * Gets the global sentiment of all reviews combined.
     *
     * @return The global sentiment emoticon
     * @author Tasmia Naomi
     */
    public String getGlobalSentiment() {
        return globalSentiment;
    }

    /**
     * Gets the most frequently occurring sentiment in reviews.
     *
     * @return The most common sentiment emoticon
     * @author Tasmia Naomi
     */
    public String getMostCommonSentiment() {
        return mostCommonSentiment;
    }

    /**
     * Gets the count of happy reviews.
     *
     * @return Number of reviews with happy sentiment
     * @author Tasmia Naomi
     */
    public int getHappyCount() {
        return happyCount;
    }

    /**
     * Gets the count of sad reviews.
     *
     * @return Number of reviews with sad sentiment
     * @author Tasmia Naomi
     */
    public int getSadCount() {
        return sadCount;
    }

    /**
     * Gets the count of neutral reviews.
     *
     * @return Number of reviews with neutral sentiment
     * @author Tasmia Naomi
     */
    public int getNeutralCount() {
        return neutralCount;
    }

    /**
     * Gets the total number of reviews.
     *
     * @return Total review count
     * @author Tasmia Naomi
     */
    public int getTotalReviews() {
        return reviews.size();
    }

    /**
     * Gets the list of individual Review objects.
     *
     * @return The list of reviews
     * @author Tasmia Naomi
     */
    public List<Review> getReviews() {
        return reviews;
    }
}




