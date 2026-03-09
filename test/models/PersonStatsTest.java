package models;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Unit tests for the PersonStats model class.
 * Tests statistics calculations for a person's known-for items.
 *
 * Testing strategy:
 * - Null and empty list handling
 * - Single and multiple item statistics
 * - Limit to 50 items
 * - Zero and high values
 *
 * @author Tasmia Naomi
 */
public class PersonStatsTest {

    /**
     * Tests PersonStats with null input.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testNullInput() {
        PersonStats stats = new PersonStats(null);
        assertEquals(0, stats.getLatestItems().size());
        assertEquals(0.0, stats.getPopAvg(), 0.01);
        assertEquals(0.0, stats.getPopMin(), 0.01);
        assertEquals(0.0, stats.getPopMax(), 0.01);
        assertEquals(0.0, stats.getVoteAvg(), 0.01);
        assertEquals(0.0, stats.getVoteMin(), 0.01);
        assertEquals(0.0, stats.getVoteMax(), 0.01);
        assertEquals(0.0, stats.getCountAvg(), 0.01);
        assertEquals(0, stats.getCountMin());
        assertEquals(0, stats.getCountMax());
    }

    /**
     * Tests PersonStats with empty list.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testEmptyList() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        assertEquals(0, stats.getLatestItems().size());
        assertEquals(0.0, stats.getPopAvg(), 0.01);
        assertEquals(0, stats.getCountMin());
        assertEquals(0, stats.getCountMax());
    }

    /**
     * Tests PersonStats with a single item.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testSingleItem() {
        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "Movie A", 15.0, 7.5, 150));
        PersonStats stats = new PersonStats(items);
        assertEquals(1, stats.getLatestItems().size());
        assertEquals(15.0, stats.getPopAvg(), 0.01);
        assertEquals(15.0, stats.getPopMin(), 0.01);
        assertEquals(15.0, stats.getPopMax(), 0.01);
        assertEquals(7.5, stats.getVoteAvg(), 0.01);
        assertEquals(150.0, stats.getCountAvg(), 0.01);
        assertEquals(150, stats.getCountMin());
        assertEquals(150, stats.getCountMax());
    }

    /**
     * Tests PersonStats with two items for min/max/average.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testTwoItems() {
        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "Movie A", 10.0, 8.0, 100));
        items.add(new MovieOrTVShow("2", "Movie B", 20.0, 6.0, 200));
        PersonStats stats = new PersonStats(items);
        assertEquals(2, stats.getLatestItems().size());
        assertEquals(15.0, stats.getPopAvg(), 0.01);
        assertEquals(10.0, stats.getPopMin(), 0.01);
        assertEquals(20.0, stats.getPopMax(), 0.01);
        assertEquals(7.0, stats.getVoteAvg(), 0.01);
        assertEquals(150.0, stats.getCountAvg(), 0.01);
        assertEquals(100, stats.getCountMin());
        assertEquals(200, stats.getCountMax());
    }

    /**
     * Tests that PersonStats limits results to 50 items.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testLimitTo50() {
        List<MovieOrTVShow> items = new ArrayList<>();
        for (int i = 0; i < 75; i++) {
            items.add(new MovieOrTVShow(String.valueOf(i), "Movie " + i, i * 1.0, (i % 10) * 1.0, i * 10));
        }
        PersonStats stats = new PersonStats(items);
        assertEquals(50, stats.getLatestItems().size());
    }

    /**
     * Tests PersonStats with exactly 50 items.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testExactly50Items() {
        List<MovieOrTVShow> items = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            items.add(new MovieOrTVShow(String.valueOf(i), "Movie " + i, i * 1.0, 5.0, 100));
        }
        PersonStats stats = new PersonStats(items);
        assertEquals(50, stats.getLatestItems().size());
    }

    /**
     * Tests PersonStats with zero values.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testZeroValues() {
        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "A", 0.0, 0.0, 0));
        items.add(new MovieOrTVShow("2", "B", 0.0, 0.0, 0));
        PersonStats stats = new PersonStats(items);
        assertEquals(0.0, stats.getPopAvg(), 0.01);
        assertEquals(0.0, stats.getVoteAvg(), 0.01);
        assertEquals(0.0, stats.getCountAvg(), 0.01);
    }

    /**
     * Tests getLatestItems returns correct content.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetLatestItemsContent() {
        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "Inception", 50.0, 8.8, 5000));
        items.add(new MovieOrTVShow("2", "Interstellar", 40.0, 8.6, 4000));
        PersonStats stats = new PersonStats(items);
        List<MovieOrTVShow> retrieved = stats.getLatestItems();
        assertEquals(2, retrieved.size());
        assertEquals("Inception", retrieved.get(0).getTitle());
        assertEquals("Interstellar", retrieved.get(1).getTitle());
    }
}
