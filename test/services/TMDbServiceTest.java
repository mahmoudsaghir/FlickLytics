package services;

import models.MovieOrTVShow;
import models.PersonStats;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the PersonStats model class.
 * Tests all statistics calculation methods including edge cases.
 *
 * Testing strategy:
 * - Basic statistics calculation with sample data
 * - Null input handling
 * - Empty list handling
 * - Large dataset handling (>50 items)
 * - Min/max/average calculation accuracy
 *
 * @author Syed Shahab Shah
 */
public class TMDbServiceTest {

    /**
     * Tests basic PersonStats calculation with two items.
     * Verifies that popularity, vote average, and vote count statistics are correct.
     *
     * Equivalence class: Normal two-item list
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsCalculation() {
        // Arrange: Create dummy data
        List<MovieOrTVShow> mockItems = new ArrayList<>();
        mockItems.add(new MovieOrTVShow("1", "Movie A", 10.0, 8.0, 100));
        mockItems.add(new MovieOrTVShow("2", "Movie B", 20.0, 6.0, 200));

        // Act: Create PersonStats from the data
        PersonStats stats = new PersonStats(mockItems);

        // Assert: Verify all statistics are calculated correctly
        assertEquals(15.0, stats.getPopAvg(), 0.01);
        assertEquals(10.0, stats.getPopMin(), 0.01);
        assertEquals(20.0, stats.getPopMax(), 0.01);

        assertEquals(7.0, stats.getVoteAvg(), 0.01);
        assertEquals(6.0, stats.getVoteMin(), 0.01);
        assertEquals(8.0, stats.getVoteMax(), 0.01);

        assertEquals(150.0, stats.getCountAvg(), 0.01);
        assertEquals(100, stats.getCountMin());
        assertEquals(200, stats.getCountMax());
    }

    /**
     * Tests PersonStats with null input.
     * Verifies that null is handled gracefully without throwing exceptions.
     *
     * Equivalence class: Null input
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsWithNullInput() {
        // Act: Create PersonStats with null input
        PersonStats stats = new PersonStats(null);

        // Assert: Should return empty list and zero statistics
        assertEquals(0, stats.getLatestItems().size());
        assertEquals(0.0, stats.getPopAvg(), 0.01);
        assertEquals(0.0, stats.getPopMin(), 0.01);
        assertEquals(0.0, stats.getPopMax(), 0.01);
        assertEquals(0, stats.getCountMin());
        assertEquals(0, stats.getCountMax());
    }

    /**
     * Tests PersonStats with empty list.
     * Verifies that empty lists return zero statistics.
     *
     * Equivalence class: Empty list
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsWithEmptyList() {
        // Arrange: Create empty list
        List<MovieOrTVShow> emptyList = new ArrayList<>();

        // Act: Create PersonStats from empty list
        PersonStats stats = new PersonStats(emptyList);

        // Assert: All statistics should be zero
        assertEquals(0, stats.getLatestItems().size());
        assertEquals(0.0, stats.getPopAvg(), 0.01);
        assertEquals(0.0, stats.getPopMin(), 0.01);
        assertEquals(0.0, stats.getPopMax(), 0.01);
        assertEquals(0.0, stats.getVoteAvg(), 0.01);
        assertEquals(0, stats.getCountMin());
        assertEquals(0, stats.getCountMax());
    }

    /**
     * Tests PersonStats limits results to 50 items.
     * Verifies that when given >50 items, only 50 are retained.
     *
     * Equivalence class: Large dataset (>50 items)
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsLimitsTo50Items() {
        // Arrange: Create 75 items
        List<MovieOrTVShow> largeList = new ArrayList<>();
        for (int i = 0; i < 75; i++) {
            largeList.add(new MovieOrTVShow(
                    String.valueOf(i),
                    "Movie " + i,
                    i * 1.0,
                    (i % 10) * 1.0,
                    i * 10
            ));
        }

        // Act: Create PersonStats from large list
        PersonStats stats = new PersonStats(largeList);

        // Assert: Should limit to 50 items
        assertEquals(50, stats.getLatestItems().size());
    }

    /**
     * Tests PersonStats with exactly 50 items.
     * Verifies that all 50 items are retained.
     *
     * Equivalence class: Exact 50-item boundary
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsWithExactly50Items() {
        // Arrange: Create exactly 50 items
        List<MovieOrTVShow> fiftyItems = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            fiftyItems.add(new MovieOrTVShow(
                    String.valueOf(i),
                    "Movie " + i,
                    i * 1.0,
                    (i % 10) * 1.0,
                    i * 10
            ));
        }

        // Act: Create PersonStats
        PersonStats stats = new PersonStats(fiftyItems);

        // Assert: Should keep all 50 items
        assertEquals(50, stats.getLatestItems().size());
    }

    /**
     * Tests PersonStats with single item.
     * Verifies that min, max, and average are equal for one item.
     *
     * Equivalence class: Single item
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsWithSingleItem() {
        // Arrange: Create list with one item
        List<MovieOrTVShow> singleItem = new ArrayList<>();
        singleItem.add(new MovieOrTVShow("1", "Movie A", 15.5, 7.5, 150));

        // Act: Create PersonStats
        PersonStats stats = new PersonStats(singleItem);

        // Assert: Min, max, and average should all be equal
        assertEquals(15.5, stats.getPopAvg(), 0.01);
        assertEquals(15.5, stats.getPopMin(), 0.01);
        assertEquals(15.5, stats.getPopMax(), 0.01);

        assertEquals(7.5, stats.getVoteAvg(), 0.01);
        assertEquals(7.5, stats.getVoteMin(), 0.01);
        assertEquals(7.5, stats.getVoteMax(), 0.01);

        assertEquals(150, stats.getCountMin());
        assertEquals(150, stats.getCountMax());
    }

    /**
     * Tests PersonStats with zero values.
     * Verifies that zero values are handled correctly in statistics.
     *
     * Equivalence class: Zero values
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsWithZeroValues() {
        // Arrange: Create items with zero values
        List<MovieOrTVShow> zeroItems = new ArrayList<>();
        zeroItems.add(new MovieOrTVShow("1", "Movie A", 0.0, 0.0, 0));
        zeroItems.add(new MovieOrTVShow("2", "Movie B", 0.0, 0.0, 0));

        // Act: Create PersonStats
        PersonStats stats = new PersonStats(zeroItems);

        // Assert: All statistics should be zero
        assertEquals(0.0, stats.getPopAvg(), 0.01);
        assertEquals(0.0, stats.getVoteAvg(), 0.01);
        assertEquals(0.0, stats.getCountAvg(), 0.01);
    }

    /**
     * Tests PersonStats with high values.
     * Verifies accurate calculation with large popularity and vote counts.
     *
     * Equivalence class: High values
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsWithHighValues() {
        // Arrange: Create items with high values
        List<MovieOrTVShow> highItems = new ArrayList<>();
        highItems.add(new MovieOrTVShow("1", "Blockbuster A", 1000.0, 9.5, 100000));
        highItems.add(new MovieOrTVShow("2", "Blockbuster B", 2000.0, 9.8, 200000));

        // Act: Create PersonStats
        PersonStats stats = new PersonStats(highItems);

        // Assert: Verify large values are handled correctly
        assertEquals(1500.0, stats.getPopAvg(), 0.01);
        assertEquals(1000.0, stats.getPopMin(), 0.01);
        assertEquals(2000.0, stats.getPopMax(), 0.01);
        assertEquals(150000.0, stats.getCountAvg(), 0.01);
    }

    /**
     * Tests PersonStats list immutability.
     * Verifies that the returned list reflects the items properly.
     *
     * Equivalence class: List content verification
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsListContent() {
        // Arrange: Create test data
        List<MovieOrTVShow> items = new ArrayList<>();
        MovieOrTVShow item1 = new MovieOrTVShow("1", "Movie A", 10.0, 8.0, 100);
        MovieOrTVShow item2 = new MovieOrTVShow("2", "Movie B", 20.0, 7.0, 200);
        items.add(item1);
        items.add(item2);

        // Act: Create PersonStats
        PersonStats stats = new PersonStats(items);

        // Assert: Verify items are in the stats
        List<MovieOrTVShow> retrievedItems = stats.getLatestItems();
        assertEquals(2, retrievedItems.size());
        assertEquals("Movie A", retrievedItems.get(0).getTitle());
        assertEquals("Movie B", retrievedItems.get(1).getTitle());
    }

    /**
     * Tests PersonStats with mixed value ranges.
     * Verifies statistics accuracy with varied data.
     *
     * Equivalence class: Mixed range values
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsWithMixedValues() {
        // Arrange: Create items with diverse values
        List<MovieOrTVShow> mixedItems = new ArrayList<>();
        mixedItems.add(new MovieOrTVShow("1", "Movie A", 5.0, 3.0, 50));
        mixedItems.add(new MovieOrTVShow("2", "Movie B", 50.0, 8.0, 5000));
        mixedItems.add(new MovieOrTVShow("3", "Movie C", 25.0, 5.0, 2500));

        // Act: Create PersonStats
        PersonStats stats = new PersonStats(mixedItems);

        // Assert: Verify correct statistics with mixed values
        assertEquals(26.67, stats.getPopAvg(), 0.1);
        assertEquals(5.33, stats.getVoteAvg(), 0.1);
        assertEquals(2516.67, stats.getCountAvg(), 1.0);
    }
}