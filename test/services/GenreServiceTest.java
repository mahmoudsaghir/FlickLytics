package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Utils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit test class for {@link GenreService}.
 *
 * <p>This test class verifies the behavior of {@link GenreService}, specifically
 * the {@code loadGenres} method, ensuring that it correctly loads genres for movies
 * and TV shows from JSON responses.</p>
 *
 * <p>The test uses Mockito to mock the static {@link Utils#sendGetRequest} method,
 * so no actual HTTP requests are performed.</p>
 * @author Mahmoud Saghir
 */
public class GenreServiceTest {

    private GenreService genreService;

    /**
     * Sets up the {@link GenreService} instance before each test.
     * @author Mahmoud Saghir
     */
    @Before
    public void setUp() {
        genreService = new GenreService();
    }

    /**
     * Tests the {@code loadGenres} method.
     *
     * <p>This test mocks JSON responses for movie and TV genres, then verifies that
     * {@code loadGenres} correctly populates the provided maps with genre IDs and names.</p>
     *
     * @throws Exception if JSON parsing fails
     * @author Mahmoud Saghir
     */
    @Test
    public void testLoadGenres() throws Exception {
        // Prepare mock JSON responses for movie and tv genres
        String movieJson = "{ \"genres\": [ {\"id\": 1, \"name\": \"Action\"}, {\"id\": 2, \"name\": \"Comedy\"} ] }";
        String tvJson = "{ \"genres\": [ {\"id\": 10, \"name\": \"Drama\"}, {\"id\": 20, \"name\": \"Reality\"} ] }";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode movieNode = mapper.readTree(movieJson);
        JsonNode tvNode = mapper.readTree(tvJson);

        Map<Integer, String> movieGenres = new HashMap<>();
        Map<Integer, String> tvGenres = new HashMap<>();

        // Mock the static method Utils.sendGetRequest
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            mockedUtils.when(() -> Utils.sendGetRequest("https://api.example.com/genre/movie/list", "token123"))
                    .thenReturn(movieNode);
            mockedUtils.when(() -> Utils.sendGetRequest("https://api.example.com/genre/tv/list", "token123"))
                    .thenReturn(tvNode);

            // Call the method under test
            genreService.loadGenres("https://api.example.com/", "token123", movieGenres, tvGenres);
        }

        // Verify movie genres
        assertEquals(2, movieGenres.size());
        assertEquals("Action", movieGenres.get(1));
        assertEquals("Comedy", movieGenres.get(2));

        // Verify tv genres
        assertEquals(2, tvGenres.size());
        assertEquals("Drama", tvGenres.get(10));
        assertEquals("Reality", tvGenres.get(20));
    }
}