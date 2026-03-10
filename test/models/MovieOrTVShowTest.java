package models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the MovieOrTVShow model class.
 * Tests both constructors and all getter methods.
 *
 * Testing strategy:
 * - Constructor 1: Basic constructor without ID
 * - Constructor 2: Full constructor with ID
 * - All getter methods: Verify data access
 * - Edge cases: Null values, zero values, large values
 *
 * @author Syed Shahab Shah
 */
public class MovieOrTVShowTest {

    /**
     * Tests the basic constructor without ID.
     * Verifies all fields are set correctly and ID is null.
     *
     * Equivalence class: Basic constructor usage
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testBasicConstructor() {
        // Arrange
        String title = "Inception";
        double popularity = 15.5;
        double voteAverage = 8.8;
        int voteCount = 5000;

        // Act
        MovieOrTVShow movie = new MovieOrTVShow(title, popularity, voteAverage, voteCount);

        // Assert
        assertEquals(title, movie.getTitle());
        assertEquals(popularity, movie.getPopularity(), 0.01);
        assertEquals(voteAverage, movie.getVoteAverage(), 0.01);
        assertEquals(voteCount, movie.getVoteCount());
        assertNull(movie.getId()); // ID should be null with basic constructor
    }

    /**
     * Tests the full constructor with ID.
     * Verifies all fields including ID are set correctly.
     *
     * Equivalence class: Full constructor usage
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testFullConstructor() {
        // Arrange
        String id = "550";
        String title = "Fight Club";
        double popularity = 25.5;
        double voteAverage = 8.8;
        int voteCount = 10000;

        // Act
        MovieOrTVShow movie = new MovieOrTVShow(id, title, popularity, voteAverage, voteCount);

        // Assert
        assertEquals(id, movie.getId());
        assertEquals(title, movie.getTitle());
        assertEquals(popularity, movie.getPopularity(), 0.01);
        assertEquals(voteAverage, movie.getVoteAverage(), 0.01);
        assertEquals(voteCount, movie.getVoteCount());
    }

    /**
     * Tests getter methods individually.
     * Verifies that each getter returns the correct value.
     *
     * Equivalence class: Individual getter verification
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testAllGetters() {
        // Arrange
        String id = "100";
        String title = "The Matrix";
        double popularity = 30.0;
        double voteAverage = 8.7;
        int voteCount = 15000;

        MovieOrTVShow movie = new MovieOrTVShow(id, title, popularity, voteAverage, voteCount);

        // Act & Assert
        assertEquals("100", movie.getId());
        assertEquals("The Matrix", movie.getTitle());
        assertEquals(30.0, movie.getPopularity(), 0.01);
        assertEquals(8.7, movie.getVoteAverage(), 0.01);
        assertEquals(15000, movie.getVoteCount());
    }

    /**
     * Tests constructor with zero values.
     * Verifies that zero values are handled correctly.
     *
     * Equivalence class: Zero values
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testConstructorWithZeroValues() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("1", "Unknown", 0.0, 0.0, 0);

        // Assert
        assertEquals(0.0, movie.getPopularity(), 0.01);
        assertEquals(0.0, movie.getVoteAverage(), 0.01);
        assertEquals(0, movie.getVoteCount());
    }

    /**
     * Tests constructor with high values.
     * Verifies that large numbers are handled correctly.
     *
     * Equivalence class: High values
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testConstructorWithHighValues() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("1", "Blockbuster", 1000.5, 9.9, 1000000);

        // Assert
        assertEquals(1000.5, movie.getPopularity(), 0.01);
        assertEquals(9.9, movie.getVoteAverage(), 0.01);
        assertEquals(1000000, movie.getVoteCount());
    }

    /**
     * Tests constructor with empty string title.
     * Verifies that empty strings are handled.
     *
     * Equivalence class: Empty string
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testConstructorWithEmptyTitle() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("", "", 5.0, 5.0, 100);

        // Assert
        assertEquals("", movie.getTitle());
        assertEquals("", movie.getId());
    }

    /**
     * Tests multiple instances are independent.
     * Verifies that creating multiple instances doesn't cause interference.
     *
     * Equivalence class: Multiple instances
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testMultipleInstancesIndependence() {
        // Act
        MovieOrTVShow movie1 = new MovieOrTVShow("1", "Movie A", 10.0, 8.0, 100);
        MovieOrTVShow movie2 = new MovieOrTVShow("2", "Movie B", 20.0, 7.0, 200);

        // Assert: Verify each instance retains its own data
        assertEquals("Movie A", movie1.getTitle());
        assertEquals("Movie B", movie2.getTitle());
        assertEquals(10.0, movie1.getPopularity(), 0.01);
        assertEquals(20.0, movie2.getPopularity(), 0.01);
    }

    /**
     * Tests constructor with special characters in title.
     * Verifies that special characters are preserved.
     *
     * Equivalence class: Special characters
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testConstructorWithSpecialCharactersInTitle() {
        // Act
        String specialTitle = "Title: The Story & More!";
        MovieOrTVShow movie = new MovieOrTVShow("1", specialTitle, 5.0, 5.0, 100);

        // Assert
        assertEquals(specialTitle, movie.getTitle());
    }

    /**
     * Tests constructor with fractional vote count values.
     * Verifies that vote count is properly cast to integer.
     *
     * Equivalence class: Integer boundary
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testConstructorWithIntegerVoteCount() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("1", "Movie", 10.0, 8.0, Integer.MAX_VALUE);

        // Assert
        assertEquals(Integer.MAX_VALUE, movie.getVoteCount());
    }

    /**
     * Tests vote average boundary values (0-10 scale).
     * Verifies that vote average values at boundaries are handled.
     *
     * Equivalence class: Vote average boundaries
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testVoteAverageBoundaries() {
        // Act: Test minimum
        MovieOrTVShow minMovie = new MovieOrTVShow("1", "Worst", 1.0, 0.0, 100);

        // Act: Test maximum
        MovieOrTVShow maxMovie = new MovieOrTVShow("2", "Best", 1.0, 10.0, 100);

        // Assert
        assertEquals(0.0, minMovie.getVoteAverage(), 0.01);
        assertEquals(10.0, maxMovie.getVoteAverage(), 0.01);
    }

    /**
     * Tests the 6-arg constructor with year.
     * Verifies all fields including year are set correctly.
     *
     * Equivalence class: Full constructor with year
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testConstructorWithYear() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("550", "Fight Club", 25.5, 8.8, 10000, "1999");

        // Assert
        assertEquals("550", movie.getId());
        assertEquals("Fight Club", movie.getTitle());
        assertEquals(25.5, movie.getPopularity(), 0.01);
        assertEquals(8.8, movie.getVoteAverage(), 0.01);
        assertEquals(10000, movie.getVoteCount());
        assertEquals("1999", movie.getYear());
    }

    /**
     * Tests that the basic constructor defaults year to empty string.
     *
     * Equivalence class: Year default for basic constructor
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testBasicConstructorDefaultsYearToEmpty() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("Inception", 15.5, 8.8, 5000);

        // Assert
        assertEquals("", movie.getYear());
    }

    /**
     * Tests that the 5-arg constructor defaults year to empty string.
     *
     * Equivalence class: Year default for 5-arg constructor
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testFiveArgConstructorDefaultsYearToEmpty() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("1", "Inception", 15.5, 8.8, 5000);

        // Assert
        assertEquals("", movie.getYear());
    }

    /**
     * Tests the 6-arg constructor with null year.
     * Verifies null year is converted to empty string.
     *
     * Equivalence class: Null year handling
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testConstructorWithNullYear() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("1", "Movie", 10.0, 8.0, 100, null);

        // Assert
        assertEquals("", movie.getYear());
    }

    /**
     * Tests the 6-arg constructor with empty year.
     *
     * Equivalence class: Empty year
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testConstructorWithEmptyYear() {
        // Act
        MovieOrTVShow movie = new MovieOrTVShow("1", "Movie", 10.0, 8.0, 100, "");

        // Assert
        assertEquals("", movie.getYear());
    }
}

