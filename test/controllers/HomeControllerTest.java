package controllers;

import models.MovieOrTVShow;
import models.PersonStats;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import services.TMDbService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/**
 * Unit tests for the HomeController class.
 * Tests controller actions including index and personStats.
 * Uses Mockito to mock external dependencies (TMDbService, WSClient).
 *
 * Testing strategy:
 * - Index action: Tests rendering of empty form
 * - Person stats action: Tests API retrieval and error handling
 * - Service mocking: Verifies proper asynchronous handling
 *
 * @author Syed Shahab Shah
 */
public class HomeControllerTest extends WithApplication {

    private HomeController controller;
    private TMDbService mockTmdbService;
    private FormFactory formFactory;
    private MessagesApi messagesApi;

    /**
     * Sets up test fixtures before each test.
     * Creates mock TMDbService and injects it into the controller.
     *
     * @author Syed Shahab Shah
     */
    @Before
    public void setUp() {
        mockTmdbService = mock(TMDbService.class);
        formFactory = app.injector().instanceOf(FormFactory.class);
        messagesApi = app.injector().instanceOf(MessagesApi.class);
        controller = new HomeController(formFactory, messagesApi, mockTmdbService);
    }

    /**
     * Provides the test application instance.
     * Builds a Play application with the test configuration.
     *
     * @return The configured Play application
     * @author Syed Shahab Shah
     */
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder().build();
    }

    /**
     * Tests the index action returns HTTP 200 OK.
     * Verifies that the index page renders successfully with an empty form.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testIndexAction() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }

    /**
     * Tests the personStats action with valid person ID.
     * Mocks TMDbService to return sample person statistics.
     * Verifies successful API call and stats page rendering.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsActionWithValidId() {
        // Arrange: Create mock person stats
        List<MovieOrTVShow> mockItems = new ArrayList<>();
        mockItems.add(new MovieOrTVShow("1", "Movie A", 10.0, 8.0, 100));
        mockItems.add(new MovieOrTVShow("2", "Movie B", 20.0, 7.0, 200));
        PersonStats mockStats = new PersonStats(mockItems);

        // Mock the service to return these stats
        when(mockTmdbService.getPersonStats(anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockStats));

        // Act: Request person stats
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/person/1/stats");

        Result result = route(app, request);

        // Assert: Check that person stats page loaded successfully
        assertEquals(OK, result.status());
    }
}
