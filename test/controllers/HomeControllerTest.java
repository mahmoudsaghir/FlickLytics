package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import models.GlobalDiversityResult;
import models.MovieOrTVShow;
import models.PersonStats;
import models.Review;
import models.ReviewsSummary;
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
import services.ReviewsService;
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
 * Tests controller actions including index, personStats, globalDiversity,
 * financial performance, and reviews.
 * Uses Mockito to mock external dependencies (TmdbService, GlobalDiversityService, ReviewsService).
 *
 * @author Syed Shahab Shah
 * @author Mahmoud Saghir
 * @author Charles Wang
 * @author Tasmia Naomi
 */
public class HomeControllerTest {
    private HomeController controller;
    private TmdbService tmdbService;
    private GlobalDiversityService globalDiversityService;
    private MediaDetailsService mediaDetailsService;
    private ReviewsService reviewsService;
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
        mediaDetailsService = mock(MediaDetailsService.class);
        reviewsService = mock(ReviewsService.class);
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
                mediaDetailsService,
                reviewsService
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

    /**
     * Tests the financialPerformance action with a successful API response.
     * Mocks TmdbService to return budget and revenue data.
     *
     * @author Charles Wang
     * @author Tasmia Naomi
     */
    @Test
    public void testFinancialPerformanceSuccess() throws Exception {
        JsonNode mockDetails = Json.newObject()
                .put("budget", 100000)
                .put("revenue", 500000);

        when(tmdbService.getDetails(anyString(), anyString(), eq("movie"), eq(123L)))
                .thenReturn(mockDetails);

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/flicklytics/financial-performance/123");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.financialPerformance(request, 123);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Financial Performance"));
    }

    /**
     * Tests the financialPerformance action when the API fails.
     * Should return an internal server error response.
     *
     * @author Charles Wang
     * @author Tasmia Naomi
     */
    @Test
    public void testFinancialPerformanceFailure() throws Exception {
        when(tmdbService.getDetails(anyString(), anyString(), eq("movie"), eq(999L)))
                .thenThrow(new RuntimeException("API failure"));

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/flicklytics/financial-performance/999");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.financialPerformance(request, 999);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
        String content = contentAsString(result);
        assertTrue(content.contains("Failed to fetch movie financial data"));
    }

    /**
     * Tests the reviews action with a successful API response.
     * Mocks ReviewsService to return a ReviewsSummary with happy reviews.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testReviewsActionSuccess() throws Exception {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("John", "Amazing and wonderful movie!", ":-)", 100.0, 0.0));
        reviews.add(new Review("Jane", "Excellent and fantastic film!", ":-)", 100.0, 0.0));
        ReviewsSummary mockSummary = new ReviewsSummary(reviews);

        when(reviewsService.getReviewsWithSentiment(anyString(), anyString(), eq("movie"), eq(123L)))
                .thenReturn(mockSummary);

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/flicklytics/reviews/movie/123");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.reviews("movie", 123L, request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Reviews"));
        assertTrue(html.contains("John"));
        assertTrue(html.contains("Jane"));
        assertTrue(html.contains(":-)"));
    }

    /**
     * Tests the reviews action when the ReviewsService throws an exception.
     * Should return an internal server error response.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testReviewsActionFailure() throws Exception {
        when(reviewsService.getReviewsWithSentiment(anyString(), anyString(), eq("movie"), eq(999L)))
                .thenThrow(new RuntimeException("API failure"));

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/flicklytics/reviews/movie/999");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.reviews("movie", 999L, request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(INTERNAL_SERVER_ERROR, result.status());
    }

    /**
     * Tests the reviews action for TV shows.
     * Verifies the controller handles TV show reviews correctly.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testReviewsActionForTvShow() throws Exception {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review("Reviewer1", "Terrible and awful show.", ":-(", 0.0, 100.0));
        ReviewsSummary mockSummary = new ReviewsSummary(reviews);

        when(reviewsService.getReviewsWithSentiment(anyString(), anyString(), eq("tv"), eq(456L)))
                .thenReturn(mockSummary);

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/flicklytics/reviews/tv/456");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.reviews("tv", 456L, request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Reviews"));
        assertTrue(html.contains("Reviewer1"));
        assertTrue(html.contains(":-("));
    }

    /**
     * Tests the reviews action with empty reviews.
     * Should render the reviews page with "No reviews found" message.
     *
     * @author Tasmia Naomi
     */
    @Test
    public void testReviewsActionWithEmptyReviews() throws Exception {
        List<Review> reviews = new ArrayList<>();
        ReviewsSummary mockSummary = new ReviewsSummary(reviews);

        when(reviewsService.getReviewsWithSentiment(anyString(), anyString(), eq("movie"), eq(789L)))
                .thenReturn(mockSummary);

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/flicklytics/reviews/movie/789");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.reviews("movie", 789L, request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("No reviews found"));
    }
}

