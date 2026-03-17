package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.GlobalDiversityResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test class for {@link GlobalDiversityService}.
 *
 * <p>This test class verifies the behavior of {@link GlobalDiversityService}, specifically
 * the {@code compute} method, ensuring that it correctly calculates the
 * {@code translationDensity} and {@code localizationIndex} based on movie or TV
 * details and translation data.</p>
 *
 * <p>The test uses sample JSON data to simulate details and translations for a media item
 * and verifies the computed results against expected values.</p>
 * @author Mahmoud Saghir
 */
public class GlobalDiversityServiceTest {

    /**
     * Tests the {@code compute} method of {@link GlobalDiversityService}.
     *
     * <p>This test provides mock JSON data for a media item's details and translations,
     * calls the {@code compute} method, and verifies that the returned
     * {@link GlobalDiversityResult} has the correct translation density,
     * localization index, and media name.</p>
     *
     * @throws Exception if JSON parsing fails
     * @author Mahmoud Saghir
     */
    @Test
    public void testCompute() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // detailsAndTranslationsRoot JSON
        String detailsAndTranslationsJson = """
                {
                    "overview": "abcd",
                    "title": "MovieA",
                    "translations": {
                        "translations": [
                            {
                                "data": {
                                    "overview": "ab"
                                }
                            },
                            {
                                "data": {
                                    "overview": "abcd"
                                }
                            }
                        ]
                    }
                }
                """;

        JsonNode detailsAndTranslationsRoot = mapper.readTree(detailsAndTranslationsJson);

        GlobalDiversityService service = new GlobalDiversityService();

        GlobalDiversityResult result = service.compute(
                "movie",
                detailsAndTranslationsRoot,
                10
        );

        // Translation Density
        assertEquals(0.2, result.translationDensity, 0.0001);
        // Localization Index
        assertEquals(0.75, result.localizationIndex, 0.0001);
        // Media name
        assertEquals("MovieA", result.mediaName);
    }
}