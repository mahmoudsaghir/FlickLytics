package models;

/**
 * Represents a single review from the TMDb API with sentiment analysis.
 * Contains the review content, author information, and computed sentiment.
 *
 * @author Tasmia Naomi
 */
public class Review {
    private final String author;
    private final String content;
    private final String sentiment;
    private final double happyPercentage;
    private final double sadPercentage;

    /**
     * Constructs a Review object with sentiment analysis results.
     *
     * @param author The author of the review
     * @param content The review content/text
     * @param sentiment The computed sentiment (":-)", ":-(", or ":-|")
     * @param happyPercentage The percentage of happy words in the review
     * @param sadPercentage The percentage of sad words in the review
     * @author Tasmia Naomi
     */
    public Review(String author, String content, String sentiment, double happyPercentage, double sadPercentage) {
        this.author = author;
        this.content = content;
        this.sentiment = sentiment;
        this.happyPercentage = happyPercentage;
        this.sadPercentage = sadPercentage;
    }

    /**
     * Gets the author of this review.
     *
     * @return The review author's name
     * @author Tasmia Naomi
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Gets the content/text of this review.
     *
     * @return The review text
     * @author Tasmia Naomi
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the computed sentiment emoticon.
     *
     * @return The sentiment (":-)", ":-(", or ":-|")
     * @author Tasmia Naomi
     */
    public String getSentiment() {
        return sentiment;
    }

    /**
     * Gets the percentage of happy words in this review.
     *
     * @return Happy words percentage (0-100)
     * @author Tasmia Naomi
     */
    public double getHappyPercentage() {
        return happyPercentage;
    }

    /**
     * Gets the percentage of sad words in this review.
     *
     * @return Sad words percentage (0-100)
     * @author Tasmia Naomi
     */
    public double getSadPercentage() {
        return sadPercentage;
    }
}




