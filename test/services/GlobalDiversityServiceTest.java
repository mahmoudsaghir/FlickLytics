package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.GlobalDiversityResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GlobalDiversityServiceTest {

    @Test
    public void testCompute() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // detailsRoot JSON
        String detailsJson = """
                {
                    "overview": "abcd",
                    "title": "MovieA"
                }
                """;

        JsonNode detailsRoot = mapper.readTree(detailsJson);

        // translations JSON
        String translationsJson = """
                {
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
                """;

        JsonNode translationRoot = mapper.readTree(translationsJson);

        GlobalDiversityService service = new GlobalDiversityService();

        GlobalDiversityResult result = service.compute(
                "movie",
                detailsRoot,
                translationRoot,
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