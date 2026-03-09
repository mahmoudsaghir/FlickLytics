package models;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Analyzes the sentiment of text content using happy and sad word lists.
 * Classifies sentiment as happy (:-)), sad (:-(), or neutral (:-|) based on
 * the percentage of happy/sad words found in the text.
 *
 * @author Tasmia Naomi
 */
public class ReviewSentimentAnalyzer {

    // Happy words list - words that indicate positive sentiment
    private static final Set<String> HAPPY_WORDS = new HashSet<>(Arrays.asList(
        "good", "great", "excellent", "amazing", "awesome", "wonderful", "fantastic",
        "love", "loves", "loved", "loving", "beautiful", "brilliant", "superb",
        "outstanding", "perfect", "incredible", "fabulous", "terrific", "delightful",
        "marvelous", "splendid", "magnificent", "happy", "happily", "joy", "joyful",
        "fun", "funny", "entertaining", "thrilling", "exciting", "impressed", "best",
        "best", "awesome", "cool", "nice", "lovely", "pleasant", "charming", "bright",
        "glad", "glad", "cheerful", "uplifting", "inspiring", "masterpiece", "genius",
        "profound", "clever", "witty", "clever", "smart", "intelligent", "brilliant",
        "recommend", "worth", "enjoyable", "engrossing", "riveting", "gripping",
        "fantastic", "phenomenal", "stellar", "superior", "excellent", "exceptional"
    ));

    // Sad words list - words that indicate negative sentiment
    private static final Set<String> SAD_WORDS = new HashSet<>(Arrays.asList(
        "bad", "terrible", "awful", "horrible", "dreadful", "poor", "waste",
        "wasted", "boring", "bored", "boring", "slow", "dull", "tedious",
        "annoying", "annoyed", "irritating", "irritated", "frustrating",
        "frustrated", "disappointing", "disappointed", "disappointment",
        "hate", "hated", "hates", "hating", "disgusting", "disgusted",
        "ugly", "worst", "pathetic", "ridiculous", "absurd", "stupid",
        "dumb", "idiotic", "pointless", "senseless", "meaningless",
        "forgettable", "unmemorable", "forgettable", "forgettable",
        "sad", "sadness", "depressing", "depressed", "dark", "gloomy",
        "bleak", "miserable", "horrible", "dreadful", "tragic", "tragic",
        "fail", "failed", "failure", "crash", "crashing", "broken",
        "unwatchable", "unbearable", "painful", "cringe", "cringing",
        "weak", "poorly", "bad", "bland", "flawed", "mediocre", "inferior"
    ));

    /**
     * Counts the number of happy and sad words in the given text content.
     * Splits text by non-alphanumeric characters and matches against word lists.
     * Returns an array where index 0 is the happy count and index 1 is the sad count.
     *
     * @param content The text to analyze
     * @return An int array of size 2: [happyCount, sadCount]
     * @author Tasmia Naomi
     */
    static int[] countSentimentWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new int[]{0, 0};
        }

        String lowerContent = content.toLowerCase();
        String[] words = lowerContent.split("[^a-z0-9]+");

        int happyCount = 0;
        int sadCount = 0;

        for (String word : words) {
            if (word.isEmpty()) continue;
            if (HAPPY_WORDS.contains(word)) {
                happyCount++;
            } else if (SAD_WORDS.contains(word)) {
                sadCount++;
            }
        }

        return new int[]{happyCount, sadCount};
    }

    /**
     * Analyzes the sentiment of the given text content.
     * Returns a sentiment emoticon based on the percentage of happy/sad words.
     *
     * Sentiment determination:
     * - If happy words > 70%: return ":-)"
     * - If sad words > 70%: return ":−("
     * - Otherwise: return ":-|"
     *
     * @param content The review text to analyze
     * @return The sentiment emoticon (":-)", ":-(", or ":-|")
     * @author Tasmia Naomi
     */
    public static String analyzeSentiment(String content) {
        int[] counts = countSentimentWords(content);
        int happyCount = counts[0];
        int sadCount = counts[1];
        int totalSentimentWords = happyCount + sadCount;

        if (totalSentimentWords == 0) {
            return ":-|";
        }

        double happyPercentage = (double) happyCount / totalSentimentWords * 100;
        double sadPercentage = (double) sadCount / totalSentimentWords * 100;

        if (happyPercentage > 70) {
            return ":-)";
        } else if (sadPercentage > 70) {
            return ":-(";
        } else {
            return ":-|";
        }
    }

    /**
     * Calculates the happy word percentage in the given text.
     *
     * @param content The review text
     * @return The percentage of happy words (0-100)
     * @author Tasmia Naomi
     */
    public static double getHappyPercentage(String content) {
        int[] counts = countSentimentWords(content);
        int totalSentimentWords = counts[0] + counts[1];
        if (totalSentimentWords == 0) {
            return 0.0;
        }
        return (double) counts[0] / totalSentimentWords * 100;
    }

    /**
     * Calculates the sad word percentage in the given text.
     *
     * @param content The review text
     * @return The percentage of sad words (0-100)
     * @author Tasmia Naomi
     */
    public static double getSadPercentage(String content) {
        int[] counts = countSentimentWords(content);
        int totalSentimentWords = counts[0] + counts[1];
        if (totalSentimentWords == 0) {
            return 0.0;
        }
        return (double) counts[1] / totalSentimentWords * 100;
    }
}



