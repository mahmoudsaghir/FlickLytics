package services;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.Test;
import play.libs.Json;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class MediaDetailsServiceTest {

    @Test
    public void getDetailsWithReadability_returnsDetailsOverviewAndScores() throws Exception {
        TmdbService tmdbService = mock(TmdbService.class);
        MediaDetailsService service = new MediaDetailsService(tmdbService);

        String apiUrl = "https://api.themoviedb.org/3/";
        String token = "test-token";
        String type = "movie";
        Long id = 123L;

        String overview = "A young hero travels across the world to find his family.";
        JsonNode details = Json.newObject()
                .put("id", 123)
                .put("overview", overview);

        when(tmdbService.getDetails(apiUrl, token, type, id)).thenReturn(details);

        MediaDetailsService.DetailsResult result =
                service.getDetailsWithReadability(apiUrl, token, type, id);

        assertNotNull(result);
        assertSame(details, result.details);
        assertEquals(overview, result.overview);
        assertNotNull(result.scores);
        assertTrue(result.scores.sentences > 0);
        assertTrue(result.scores.words > 0);
        assertTrue(result.scores.syllables > 0);

        verify(tmdbService, times(1)).getDetails(apiUrl, token, type, id);
    }

    @Test
    public void getDetailsWithReadability_handlesEmptyOverview() throws Exception {
        TmdbService tmdbService = mock(TmdbService.class);
        MediaDetailsService service = new MediaDetailsService(tmdbService);

        String apiUrl = "https://api.themoviedb.org/3/";
        String token = "test-token";
        String type = "tv";
        Long id = 456L;

        JsonNode details = Json.newObject()
                .put("id", 456);

        when(tmdbService.getDetails(apiUrl, token, type, id)).thenReturn(details);

        MediaDetailsService.DetailsResult result =
                service.getDetailsWithReadability(apiUrl, token, type, id);

        assertNotNull(result);
        assertSame(details, result.details);
        assertEquals("", result.overview);
        assertNotNull(result.scores);
        assertEquals(0, result.scores.sentences);
        assertEquals(0, result.scores.words);
        assertEquals(0, result.scores.syllables);
        assertEquals(0.0, result.scores.fleschReadingEase,0.001);
        assertEquals(0.0, result.scores.fleschKincaidGrade,0.001);

        verify(tmdbService, times(1)).getDetails(apiUrl, token, type, id);
    }

    @Test
    public void getDetailsWithReadability_throwsWhenTmdbServiceFails() throws Exception {
        TmdbService tmdbService = mock(TmdbService.class);
        MediaDetailsService service = new MediaDetailsService(tmdbService);

        String apiUrl = "https://api.themoviedb.org/3/";
        String token = "test-token";
        String type = "movie";
        Long id = 789L;

        when(tmdbService.getDetails(apiUrl, token, type, id))
                .thenThrow(new Exception("TMDb failed"));

        Exception ex = assertThrows(
                Exception.class,
                () -> service.getDetailsWithReadability(apiUrl, token, type, id)
        );

        assertEquals("TMDb failed", ex.getMessage());

        verify(tmdbService, times(1)).getDetails(apiUrl, token, type, id);
    }
}