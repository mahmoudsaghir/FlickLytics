package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.GlobalDiversityResult;
import models.MovieOrTVShow;
import models.PersonStats;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import services.GenreService;
import services.GlobalDiversityService;
import services.MediaDetailsService;
import services.TmdbService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static play.test.Helpers.*;

/**
 * Unit tests for the HomeController class.
 * Tests controller actions including index, personStats, and globalDiversity.
 * Uses Mockito to mock external dependencies (TmdbService, GlobalDiversityService).
 *
 * Testing strategy:
 * - Index action: Tests rendering of empty form
 * - Person stats action: Tests API retrieval and error handling
 * - Global diversity action: Tests computation and rendering
 * - Service mocking: Verifies proper asynchronous handling
 *
 * @author Syed Shahab Shah
 * @author Mahmoud Saghir
 */
public class HomeControllerTest {
    private HomeController controller;
    private TmdbService tmdbService;
    private GlobalDiversityService globalDiversityService;
    private MediaDetailsService mediaDetailsService;
    /**
     * Sets up test fixtures before each test.
     * Creates mock services and injects them into the controller.
     *
     * @author Mahmoud Saghir
     * @author Syed Shahab Shah
     */
    @Before
    public void setUp() {
        tmdbService = mock(TmdbService.class);
        globalDiversityService = mock(GlobalDiversityService.class);
        GenreService genreService = mock(GenreService.class);
        Config config = mock(Config.class);

        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");

        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);

        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        controller = new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                globalDiversityService,
                genreService,
                tmdbService,
                mediaDetailsService

        );
    }

    /**
     * Tests the globalDiversity action.
     * Mocks TmdbService and GlobalDiversityService to return sample data.
     * Verifies successful rendering with computed metrics.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testGlobalDiversity() throws Exception {
        // Mock TMDb responses
        JsonNode detailsNode = Json.newObject().put("overview", "abc");
        JsonNode translationsNode = Json.newObject().set("translations", Json.newArray());

        when(tmdbService.getDetails(anyString(), anyString(), eq("movie"), eq(1L)))
                .thenReturn(detailsNode);

        when(tmdbService.getTranslations(anyString(), anyString(), eq("movie"), eq(1L)))
                .thenReturn(translationsNode);

        // Mock computed result
        GlobalDiversityResult mockResult = new GlobalDiversityResult(0.5, 0.8, "Test Movie");

        when(globalDiversityService.compute(eq("movie"), any(), any(), eq(10)))
                .thenReturn(mockResult);

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.globalDiversity(request, "movie", 1);

        Result result = resultStage.toCompletableFuture().join();
        String html = contentAsString(result);

        assertEquals(OK, result.status());
        assertTrue(html.contains("Test Movie"));
        assertTrue(html.contains("Translation Density"));
        assertTrue(html.contains("0.5"));
        assertTrue(html.contains("Localization Index"));
        assertTrue(html.contains("0.8"));
    }

    /**
     * Tests the personStats action with valid person ID.
     * Mocks TmdbService to return sample person credits.
     * Verifies successful API call and stats page rendering.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsActionWithValidId() throws Exception {
        // Arrange: Create mock combined credits JSON
        JsonNode castItem1 = Json.newObject()
                .put("id", 1).put("title", "Movie A")
                .put("popularity", 10.0).put("vote_average", 8.0).put("vote_count", 100);
        JsonNode castItem2 = Json.newObject()
                .put("id", 2).put("title", "Movie B")
                .put("popularity", 20.0).put("vote_average", 7.0).put("vote_count", 200);

        com.fasterxml.jackson.databind.node.ObjectNode creditsJson = Json.newObject();
        com.fasterxml.jackson.databind.node.ArrayNode castArray = Json.newArray();
        castArray.add(castItem1);
        castArray.add(castItem2);
        creditsJson.set("cast", castArray);
        creditsJson.set("crew", Json.newArray());

        when(tmdbService.getPersonCredits(anyString(), anyString(), eq("1")))
                .thenReturn(creditsJson);

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/person/1/stats");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.personStats("1", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Person Statistics"));
        assertTrue(html.contains("Movie A"));
        assertTrue(html.contains("Movie B"));
    }
}
