package services;

import com.fasterxml.jackson.databind.JsonNode;
import models.MovieOrTVShow;
import models.PersonStats;
import models.Utils;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    // ===== Year Sorting Tests =====

    /**
     * Tests that PersonStats sorts items by year in descending order (latest first).
     * Verifies items are ordered from newest to oldest year.
     *
     * Equivalence class: Year sorting - descending order
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsSortsByYearDescending() {
        // Arrange: Create items with different years in random order
        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "Old Movie", 10.0, 7.0, 100, "2000"));
        items.add(new MovieOrTVShow("2", "New Movie", 20.0, 8.0, 200, "2024"));
        items.add(new MovieOrTVShow("3", "Mid Movie", 15.0, 7.5, 150, "2015"));

        // Act
        PersonStats stats = new PersonStats(items);
        List<MovieOrTVShow> sorted = stats.getLatestItems();

        // Assert: Latest year first
        assertEquals("New Movie", sorted.get(0).getTitle());
        assertEquals("2024", sorted.get(0).getYear());
        assertEquals("Mid Movie", sorted.get(1).getTitle());
        assertEquals("2015", sorted.get(1).getYear());
        assertEquals("Old Movie", sorted.get(2).getTitle());
        assertEquals("2000", sorted.get(2).getYear());
    }

    /**
     * Tests year sorting with empty year values.
     * Items with empty years should appear after items with valid years.
     *
     * Equivalence class: Year sorting with empty years
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsSortingWithEmptyYears() {
        // Arrange
        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "No Year", 10.0, 7.0, 100, ""));
        items.add(new MovieOrTVShow("2", "Has Year", 20.0, 8.0, 200, "2020"));

        // Act
        PersonStats stats = new PersonStats(items);
        List<MovieOrTVShow> sorted = stats.getLatestItems();

        // Assert: Item with year should come before item without year
        assertEquals("Has Year", sorted.get(0).getTitle());
        assertEquals("No Year", sorted.get(1).getTitle());
    }

    /**
     * Tests year sorting with all same years.
     *
     * Equivalence class: Year sorting with identical years
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsSortingWithSameYears() {
        // Arrange
        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "Movie A", 10.0, 7.0, 100, "2020"));
        items.add(new MovieOrTVShow("2", "Movie B", 20.0, 8.0, 200, "2020"));

        // Act
        PersonStats stats = new PersonStats(items);

        // Assert: Both items should be present
        assertEquals(2, stats.getLatestItems().size());
    }

    // ===== PersonStats.setPersonDetails Tests =====

    /**
     * Tests setPersonDetails with valid data.
     * Verifies all person detail fields are correctly set.
     *
     * Equivalence class: Valid person details
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithValidData() {
        // Arrange
        PersonStats stats = new PersonStats(new ArrayList<>());

        // Act
        stats.setPersonDetails("Tom Hanks", "/photo.jpg", "Acting", 2, "1956-07-09", "Concord, California, USA");

        // Assert
        assertEquals("Tom Hanks", stats.getPersonName());
        assertEquals("https://image.tmdb.org/t/p/w300/photo.jpg", stats.getProfilePhotoUrl());
        assertEquals("Acting", stats.getKnownFor());
        assertEquals("Male", stats.getGender());
        assertEquals("1956-07-09", stats.getBirthday());
        assertTrue(stats.getAge() > 0);
        assertEquals("Concord, California, USA", stats.getPlaceOfBirth());
    }

    /**
     * Tests setPersonDetails with null name.
     * Verifies null name defaults to "Unknown".
     *
     * Equivalence class: Null name
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithNullName() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails(null, "/photo.jpg", "Acting", 2, "1990-01-01", "LA, USA");
        assertEquals("Unknown", stats.getPersonName());
    }

    /**
     * Tests setPersonDetails with null profile path.
     * Verifies null profile path results in empty URL.
     *
     * Equivalence class: Null profile path
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithNullProfilePath() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", null, "Acting", 2, "1990-01-01", "LA, USA");
        assertEquals("", stats.getProfilePhotoUrl());
    }

    /**
     * Tests setPersonDetails with empty profile path.
     * Verifies empty profile path results in empty URL.
     *
     * Equivalence class: Empty profile path
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithEmptyProfilePath() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "", "Acting", 2, "1990-01-01", "LA, USA");
        assertEquals("", stats.getProfilePhotoUrl());
    }

    /**
     * Tests setPersonDetails with null knownFor department.
     * Verifies null defaults to "N/A".
     *
     * Equivalence class: Null known for department
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithNullKnownFor() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", null, 2, "1990-01-01", "LA, USA");
        assertEquals("N/A", stats.getKnownFor());
    }

    /**
     * Tests setPersonDetails with gender code 1 (Female).
     *
     * Equivalence class: Female gender code
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsGenderFemale() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 1, "1990-01-01", "LA, USA");
        assertEquals("Female", stats.getGender());
    }

    /**
     * Tests setPersonDetails with gender code 2 (Male).
     *
     * Equivalence class: Male gender code
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsGenderMale() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 2, "1990-01-01", "LA, USA");
        assertEquals("Male", stats.getGender());
    }

    /**
     * Tests setPersonDetails with gender code 0 (Not specified).
     *
     * Equivalence class: Unknown gender code
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsGenderNotSpecified() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 0, "1990-01-01", "LA, USA");
        assertEquals("Not specified", stats.getGender());
    }

    /**
     * Tests setPersonDetails with null birthday.
     * Verifies null birthday defaults to "N/A" and age is -1.
     *
     * Equivalence class: Null birthday
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithNullBirthday() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 2, null, "LA, USA");
        assertEquals("N/A", stats.getBirthday());
        assertEquals(-1, stats.getAge());
    }

    /**
     * Tests setPersonDetails with empty birthday.
     * Verifies empty birthday defaults to "N/A" and age is -1.
     *
     * Equivalence class: Empty birthday
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithEmptyBirthday() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 2, "", "LA, USA");
        assertEquals("N/A", stats.getBirthday());
        assertEquals(-1, stats.getAge());
    }

    /**
     * Tests setPersonDetails with invalid birthday format.
     * Verifies invalid birthday results in age = -1.
     *
     * Equivalence class: Invalid birthday format
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithInvalidBirthday() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 2, "not-a-date", "LA, USA");
        assertEquals("not-a-date", stats.getBirthday());
        assertEquals(-1, stats.getAge());
    }

    /**
     * Tests setPersonDetails age calculation with known birthday.
     * Uses a specific date to verify age is correctly computed.
     *
     * Equivalence class: Age calculation accuracy
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsAgeCalculation() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 2, "2000-01-01", "LA, USA");

        // Person born on 2000-01-01, age should be 26 (as of March 9, 2026)
        assertEquals(26, stats.getAge());
    }

    /**
     * Tests setPersonDetails with null place of birth.
     * Verifies null defaults to "N/A".
     *
     * Equivalence class: Null place of birth
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithNullPlaceOfBirth() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 2, "1990-01-01", null);
        assertEquals("N/A", stats.getPlaceOfBirth());
    }

    /**
     * Tests setPersonDetails with empty place of birth.
     * Verifies empty defaults to "N/A".
     *
     * Equivalence class: Empty place of birth
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsWithEmptyPlaceOfBirth() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails("Test", "/p.jpg", "Acting", 2, "1990-01-01", "");
        assertEquals("N/A", stats.getPlaceOfBirth());
    }

    /**
     * Tests setPersonDetails with all null values.
     * Verifies all fields default gracefully.
     *
     * Equivalence class: All null person details
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSetPersonDetailsAllNulls() {
        PersonStats stats = new PersonStats(new ArrayList<>());
        stats.setPersonDetails(null, null, null, 0, null, null);

        assertEquals("Unknown", stats.getPersonName());
        assertEquals("", stats.getProfilePhotoUrl());
        assertEquals("N/A", stats.getKnownFor());
        assertEquals("Not specified", stats.getGender());
        assertEquals("N/A", stats.getBirthday());
        assertEquals(-1, stats.getAge());
        assertEquals("N/A", stats.getPlaceOfBirth());
    }

    /**
     * Tests PersonStats person detail getters before setPersonDetails is called.
     * Verifies getters return null when details haven't been set.
     *
     * Equivalence class: Default person detail values (unset)
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonDetailsDefaultValues() {
        PersonStats stats = new PersonStats(new ArrayList<>());

        // Before calling setPersonDetails, all should be null/0
        assertNull(stats.getPersonName());
        assertNull(stats.getProfilePhotoUrl());
        assertNull(stats.getKnownFor());
        assertNull(stats.getGender());
        assertNull(stats.getBirthday());
        assertEquals(0, stats.getAge());
        assertNull(stats.getPlaceOfBirth());
    }

    // ===== TmdbService.getPersonCredits Tests =====

    /**
     * Tests getPersonCredits builds correct URL and returns API response.
     * Mocks Utils.sendGetRequest to verify URL construction.
     *
     * Equivalence class: Valid person credits request
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testGetPersonCredits() throws Exception {
        TmdbService tmdbService = new TmdbService();
        String jsonResponse = "{\"cast\": [{\"id\": 1, \"title\": \"Movie A\"}], \"crew\": []}";
        JsonNode mockNode = play.libs.Json.parse(jsonResponse);

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(
                    eq("http://api.tmdb.org/3/person/123/combined_credits"), anyString()))
                    .thenReturn(mockNode);

            JsonNode result = tmdbService.getPersonCredits("http://api.tmdb.org/3/", "token", "123");

            assertNotNull(result);
            assertTrue(result.has("cast"));
            assertTrue(result.has("crew"));
            assertEquals(1, result.path("cast").size());
            assertEquals("Movie A", result.path("cast").get(0).path("title").asText());
        }
    }

    /**
     * Tests getPersonCredits throws exception when API fails.
     *
     * Equivalence class: API failure for person credits
     *
     * @author Syed Shahab Shah
     */
    @Test(expected = Exception.class)
    public void testGetPersonCreditsThrowsOnFailure() throws Exception {
        TmdbService tmdbService = new TmdbService();

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(anyString(), anyString()))
                    .thenThrow(new Exception("API failure"));

            tmdbService.getPersonCredits("http://api.tmdb.org/3/", "token", "123");
        }
    }

    // ===== TmdbService.getPersonDetails Tests =====

    /**
     * Tests getPersonDetails builds correct URL and returns person data.
     * Mocks Utils.sendGetRequest to verify URL construction and response parsing.
     *
     * Equivalence class: Valid person details request
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testGetPersonDetails() throws Exception {
        TmdbService tmdbService = new TmdbService();
        String jsonResponse = "{\"name\": \"Tom Hanks\", \"birthday\": \"1956-07-09\", " +
                "\"gender\": 2, \"place_of_birth\": \"Concord, California\", " +
                "\"profile_path\": \"/photo.jpg\", \"known_for_department\": \"Acting\"}";
        JsonNode mockNode = play.libs.Json.parse(jsonResponse);

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(
                    eq("http://api.tmdb.org/3/person/31"), anyString()))
                    .thenReturn(mockNode);

            JsonNode result = tmdbService.getPersonDetails("http://api.tmdb.org/3/", "token", "31");

            assertNotNull(result);
            assertEquals("Tom Hanks", result.path("name").asText());
            assertEquals("1956-07-09", result.path("birthday").asText());
            assertEquals(2, result.path("gender").asInt());
            assertEquals("Concord, California", result.path("place_of_birth").asText());
            assertEquals("/photo.jpg", result.path("profile_path").asText());
            assertEquals("Acting", result.path("known_for_department").asText());
        }
    }

    /**
     * Tests getPersonDetails throws exception when API fails.
     *
     * Equivalence class: API failure for person details
     *
     * @author Syed Shahab Shah
     */
    @Test(expected = Exception.class)
    public void testGetPersonDetailsThrowsOnFailure() throws Exception {
        TmdbService tmdbService = new TmdbService();

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(anyString(), anyString()))
                    .thenThrow(new Exception("API failure"));

            tmdbService.getPersonDetails("http://api.tmdb.org/3/", "token", "31");
        }
    }

    /**
     * Tests getReviews with a single page of results.
     * Mocks Utils.sendGetRequest to return a single page response.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsSinglePage() throws Exception {
        TmdbService tmdbService = new TmdbService();
        String jsonResponse = "{\"results\": [{\"author\": \"John\", \"content\": \"Great!\"}], \"total_pages\": 1, \"total_results\": 1}";
        JsonNode mockNode = play.libs.Json.parse(jsonResponse);

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(contains("/reviews?page=1"), anyString()))
                    .thenReturn(mockNode);

            JsonNode result = tmdbService.getReviews("http://api.tmdb.org/3/", "token", "movie", 123L);
            assertNotNull(result);
            assertTrue(result.has("results"));
            assertEquals(1, result.path("results").size());
            assertEquals("John", result.path("results").get(0).path("author").asText());
        }
    }

    /**
     * Tests getReviews with multiple pages (3 pages).
     * Verifies results from all pages are merged.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsMultiplePages() throws Exception {
        TmdbService tmdbService = new TmdbService();
        String page1 = "{\"results\": [{\"author\": \"A1\", \"content\": \"P1\"}], \"total_pages\": 3, \"total_results\": 3}";
        String page2 = "{\"results\": [{\"author\": \"A2\", \"content\": \"P2\"}], \"total_pages\": 3, \"total_results\": 3}";
        String page3 = "{\"results\": [{\"author\": \"A3\", \"content\": \"P3\"}], \"total_pages\": 3, \"total_results\": 3}";

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(contains("page=1"), anyString()))
                    .thenReturn(play.libs.Json.parse(page1));
            mockedUtils.when(() -> Utils.sendGetRequest(contains("page=2"), anyString()))
                    .thenReturn(play.libs.Json.parse(page2));
            mockedUtils.when(() -> Utils.sendGetRequest(contains("page=3"), anyString()))
                    .thenReturn(play.libs.Json.parse(page3));

            JsonNode result = tmdbService.getReviews("http://api.tmdb.org/3/", "token", "movie", 456L);
            assertNotNull(result);
            assertEquals(3, result.path("results").size());
            assertEquals("A1", result.path("results").get(0).path("author").asText());
            assertEquals("A2", result.path("results").get(1).path("author").asText());
            assertEquals("A3", result.path("results").get(2).path("author").asText());
        }
    }

    /**
     * Tests getReviews caps pagination at 3 pages even if more exist.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testGetReviewsCapsAtThreePages() throws Exception {
        TmdbService tmdbService = new TmdbService();
        String page1 = "{\"results\": [{\"author\": \"A1\"}], \"total_pages\": 5, \"total_results\": 100}";
        String page2 = "{\"results\": [{\"author\": \"A2\"}], \"total_pages\": 5, \"total_results\": 100}";
        String page3 = "{\"results\": [{\"author\": \"A3\"}], \"total_pages\": 5, \"total_results\": 100}";

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(contains("page=1"), anyString()))
                    .thenReturn(play.libs.Json.parse(page1));
            mockedUtils.when(() -> Utils.sendGetRequest(contains("page=2"), anyString()))
                    .thenReturn(play.libs.Json.parse(page2));
            mockedUtils.when(() -> Utils.sendGetRequest(contains("page=3"), anyString()))
                    .thenReturn(play.libs.Json.parse(page3));

            JsonNode result = tmdbService.getReviews("http://api.tmdb.org/3/", "token", "movie", 789L);
            assertNotNull(result);
            assertEquals(3, result.path("results").size());
            mockedUtils.verify(() -> Utils.sendGetRequest(contains("page=4"), anyString()), never());
            mockedUtils.verify(() -> Utils.sendGetRequest(contains("page=5"), anyString()), never());
        }
    }

    // ===== TmdbService.search Tests =====

    /**
     * Covers movie search URL composition and space encoding.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSearchMovieEncodesQueryAndCallsMovieEndpoint() throws Exception {
        TmdbService tmdbService = new TmdbService();
        JsonNode mockNode = play.libs.Json.parse("{\"results\": []}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(
                            eq("http://api.tmdb.org/3/search/movie?query=Spider%20Man"), eq("token")))
                    .thenReturn(mockNode);

            JsonNode result = tmdbService.search("http://api.tmdb.org/3/", "token", "Spider Man", "movie");

            assertSame(mockNode, result);
            mockedUtils.verify(() -> Utils.sendGetRequest(
                    eq("http://api.tmdb.org/3/search/movie?query=Spider%20Man"), eq("token")));
        }
    }

    /**
     * Covers tv search URL composition.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSearchTvBuildsTvEndpoint() throws Exception {
        TmdbService tmdbService = new TmdbService();
        JsonNode mockNode = play.libs.Json.parse("{\"results\": []}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(
                            eq("http://api.tmdb.org/3/search/tv?query=Breaking%20Bad"), eq("token")))
                    .thenReturn(mockNode);

            JsonNode result = tmdbService.search("http://api.tmdb.org/3/", "token", "Breaking Bad", "tv");

            assertSame(mockNode, result);
        }
    }

    /**
     * Covers person search URL composition.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSearchPersonBuildsPersonEndpoint() throws Exception {
        TmdbService tmdbService = new TmdbService();
        JsonNode mockNode = play.libs.Json.parse("{\"results\": []}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(
                            eq("http://api.tmdb.org/3/search/person?query=Tom%20Hanks"), eq("token")))
                    .thenReturn(mockNode);

            JsonNode result = tmdbService.search("http://api.tmdb.org/3/", "token", "Tom Hanks", "person");

            assertSame(mockNode, result);
        }
    }

    /**
     * Covers default switch branch where category is unsupported.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testSearchUnsupportedCategoryFallsBackToBaseSearchUrl() throws Exception {
        TmdbService tmdbService = new TmdbService();
        JsonNode mockNode = play.libs.Json.parse("{\"results\": []}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(eq("http://api.tmdb.org/3/search/"), eq("token")))
                    .thenReturn(mockNode);

            JsonNode result = tmdbService.search("http://api.tmdb.org/3/", "token", "Ignored Query", "collection");

            assertSame(mockNode, result);
            mockedUtils.verify(() -> Utils.sendGetRequest(eq("http://api.tmdb.org/3/search/"), eq("token")));
        }
    }

    /**
     * Covers exception propagation path for search.
     *
     * @author Syed Shahab Shah
     */
    @Test(expected = Exception.class)
    public void testSearchPropagatesException() throws Exception {
        TmdbService tmdbService = new TmdbService();

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(anyString(), anyString()))
                    .thenThrow(new Exception("API failure"));

            tmdbService.search("http://api.tmdb.org/3/", "token", "Any Query", "movie");
        }
    }

    // ===== TmdbService.loadTargetLanguageConstant Tests =====

    /**
     * Returns size when translations field is an array.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testLoadTargetLanguageConstantReturnsArraySize() {
        TmdbService tmdbService = new TmdbService();
        JsonNode node = play.libs.Json.parse("{\"translations\":[{\"iso_639_1\":\"en\"},{\"iso_639_1\":\"fr\"}]}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(
                            eq("http://api.tmdb.org/3/collection/10/translations"), eq("token")))
                    .thenReturn(node);

            int constant = tmdbService.loadTargetLanguageConstant("http://api.tmdb.org/3/", "token");

            assertEquals(2, constant);
            mockedUtils.verify(() -> Utils.sendGetRequest(
                    eq("http://api.tmdb.org/3/collection/10/translations"), eq("token")));
        }
    }

    /**
     * Returns fallback 1 when translations is not an array.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testLoadTargetLanguageConstantReturnsFallbackWhenNotArray() {
        TmdbService tmdbService = new TmdbService();
        JsonNode node = play.libs.Json.parse("{\"translations\":{\"iso_639_1\":\"en\"}}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(anyString(), anyString())).thenReturn(node);

            int constant = tmdbService.loadTargetLanguageConstant("http://api.tmdb.org/3/", "token");

            assertEquals(1, constant);
        }
    }

    /**
     * Returns fallback 1 when request throws exception.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testLoadTargetLanguageConstantReturnsFallbackOnException() {
        TmdbService tmdbService = new TmdbService();

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(anyString(), anyString()))
                    .thenThrow(new RuntimeException("boom"));

            int constant = tmdbService.loadTargetLanguageConstant("http://api.tmdb.org/3/", "token");

            assertEquals(1, constant);
        }
    }

    // ===== TmdbService.getDetailsAndTranslations Tests =====

    /**
     * Covers URL composition and success result passthrough for getDetailsAndTranslations.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testGetDetailsAndTranslationsBuildsExpectedUrl() throws Exception {
        TmdbService tmdbService = new TmdbService();
        JsonNode mockNode = play.libs.Json.parse("{\"id\": 100, \"translations\": {\"translations\":[]}}");

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(
                            eq("http://api.tmdb.org/3/movie/100?append_to_response=translations&language=en-US"), eq("token")))
                    .thenReturn(mockNode);

            JsonNode result = tmdbService.getDetailsAndTranslations("http://api.tmdb.org/3/", "token", "movie", 100L);

            assertSame(mockNode, result);
            mockedUtils.verify(() -> Utils.sendGetRequest(
                    eq("http://api.tmdb.org/3/movie/100?append_to_response=translations&language=en-US"), eq("token")));
        }
    }

    /**
     * Covers exception propagation for getDetailsAndTranslations.
     *
     * @author Syed Shahab Shah
     */
    @Test(expected = Exception.class)
    public void testGetDetailsAndTranslationsPropagatesException() throws Exception {
        TmdbService tmdbService = new TmdbService();

        try (MockedStatic<Utils> mockedUtils = mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest(anyString(), anyString()))
                    .thenThrow(new Exception("API failure"));

            tmdbService.getDetailsAndTranslations("http://api.tmdb.org/3/", "token", "tv", 99L);
        }
    }
}