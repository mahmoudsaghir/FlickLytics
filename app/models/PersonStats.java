package models;

import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aggregates and provides statistical analysis of a person's "known for" media items.
 * This class retrieves up to 50 of the latest items a person is known for from TMDb,
 * then calculates comprehensive statistics including popularity, vote average, and vote count
 * using Java 8+ Streams API.
 *
 * @author Syed Shahab Shah
 */
public class PersonStats {
    private final List<MovieOrTVShow> latestItems;
    private DoubleSummaryStatistics popularityStats;
    private DoubleSummaryStatistics voteAverageStats;
    private IntSummaryStatistics voteCountStats;

    /**
     * Constructs a PersonStats object from a list of all available items.
     * Automatically limits the items to the 50 most recent and calculates statistics.
     * Handles null input gracefully by using an empty list.
     *
     * @param allItems The list of all items a person is known for (can be null)
     * @author Syed Shahab Shah
     */
    public PersonStats(List<MovieOrTVShow> allItems) {
        // Guard against null to prevent crashes
        if (allItems == null) {
            this.latestItems = java.util.Collections.emptyList();
        } else {
            this.latestItems = allItems.stream()
                    .limit(50)
                    .collect(Collectors.toList());
        }

        calculateStatistics();
    }

    /**
     * Calculates summary statistics for popularity, vote average, and vote count
     * using Java 8+ Streams API's summaryStatistics() method.
     * This method is called during construction and handles empty lists gracefully.
     *
     * @author Syed Shahab Shah
     */
    private void calculateStatistics() {
        // If the list is empty, summaryStatistics() will return 0s naturally
        this.popularityStats = latestItems.stream()
                .mapToDouble(MovieOrTVShow::getPopularity)
                .summaryStatistics();

        this.voteAverageStats = latestItems.stream()
                .mapToDouble(MovieOrTVShow::getVoteAverage)
                .summaryStatistics();

        this.voteCountStats = latestItems.stream()
                .mapToInt(MovieOrTVShow::getVoteCount)
                .summaryStatistics();
    }

    /**
     * Gets the list of up to 50 latest items this person is known for.
     *
     * @return An unmodifiable list of MovieOrTVShow items
     * @author Syed Shahab Shah
     */
    public List<MovieOrTVShow> getLatestItems() {
        return latestItems;
    }

    /**
     * Gets the average popularity score across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The average popularity score
     * @author Syed Shahab Shah
     */
    public double getPopAvg() {
        return popularityStats.getAverage();
    }

    /**
     * Gets the minimum popularity score across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The minimum popularity score
     * @author Syed Shahab Shah
     */
    public double getPopMin() {
        return latestItems.isEmpty() ? 0.0 : popularityStats.getMin();
    }

    /**
     * Gets the maximum popularity score across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The maximum popularity score
     * @author Syed Shahab Shah
     */
    public double getPopMax() {
        return latestItems.isEmpty() ? 0.0 : popularityStats.getMax();
    }

    /**
     * Gets the average vote average rating across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The average of vote average ratings
     * @author Syed Shahab Shah
     */
    public double getVoteAvg() {
        return voteAverageStats.getAverage();
    }

    /**
     * Gets the minimum vote average rating across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The minimum vote average rating
     * @author Syed Shahab Shah
     */
    public double getVoteMin() {
        return latestItems.isEmpty() ? 0.0 : voteAverageStats.getMin();
    }

    /**
     * Gets the maximum vote average rating across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The maximum vote average rating
     * @author Syed Shahab Shah
     */
    public double getVoteMax() {
        return latestItems.isEmpty() ? 0.0 : voteAverageStats.getMax();
    }

    /**
     * Gets the average vote count across all items.
     * Returns 0.0 for an empty list.
     *
     * @return The average number of votes as a double
     * @author Syed Shahab Shah
     */
    public double getCountAvg() {
        return voteCountStats.getAverage();
    }

    /**
     * Gets the minimum vote count across all items.
     * Returns 0 for an empty list.
     *
     * @return The minimum vote count
     * @author Syed Shahab Shah
     */
    public int getCountMin() {
        return latestItems.isEmpty() ? 0 : voteCountStats.getMin();
    }

    /**
     * Gets the maximum vote count across all items.
     * Returns 0 for an empty list.
     *
     * @return The maximum vote count
     * @author Syed Shahab Shah
     */
    public int getCountMax() {
        return latestItems.isEmpty() ? 0 : voteCountStats.getMax();
    }
}