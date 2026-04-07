package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import models.GlobalDiversityResult;
import models.MovieOrTVShow;
import models.Review;
import models.ReviewsSummary;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.pattern.Patterns;
import org.apache.pekko.stream.Materializer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import play.Application;
import play.api.routing.JavaScriptReverseRoute;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import scala.runtime.AbstractFunction0;
import services.*;

import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
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
    private MediaDetailsService mediaDetailsService;
    private ReviewsService reviewsService;

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
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
        mediaDetailsService = mock(MediaDetailsService.class);
        reviewsService = mock(ReviewsService.class);
        GenreService genreService = mock(GenreService.class);
        Config config = mock(Config.class);

        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");

        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);

        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        ActorSystem actorSystem = testKit.system().classicSystem();
        ActorRef supervisorActor = mock(ActorRef.class);
        Materializer materializer = application.injector().instanceOf(Materializer.class);

        controller = new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                genreService,
                tmdbService,
                mediaDetailsService,
                reviewsService,
                actorSystem,
                supervisorActor,
                materializer,
                application.injector().instanceOf(MediaStreamService.class)
        );
    }

    /**
     * Tests the globalDiversity action.
     * Mocks TmdbService and actor ask to return sample data.
     * Verifies successful rendering with computed metrics.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testGlobalDiversity() throws Exception {
        // Mock TMDb responses
        JsonNode detailsNode = Json.newObject().put("overview", "abc");
        JsonNode translationsNode = Json.newObject().set("translations", Json.newArray());

        when(tmdbService.getDetailsAndTranslations(anyString(), anyString(), eq("movie"), eq(1L)))
                .thenReturn(detailsNode);

        // Mock actor response instead of service
        GlobalDiversityResult mockResult = new GlobalDiversityResult(0.5, 0.8, "Test Movie");
        CompletionStage<Object> future = CompletableFuture.completedFuture(mockResult);

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/");
        Http.Request request = requestBuilder.build();

        try (MockedStatic<Patterns> mockedPatterns = Mockito.mockStatic(org.apache.pekko.pattern.Patterns.class)) {
            mockedPatterns.when(() -> Patterns.ask(any(ActorRef.class), any(), any(java.time.Duration.class))).thenReturn(future);

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

        when(tmdbService.getPersonCredits(anyString(), anyString(), eq("2")))
                .thenReturn(creditsJson);

        // Mock person details response
        com.fasterxml.jackson.databind.node.ObjectNode personDetailsJson = Json.newObject();
        personDetailsJson.put("name", "Test Person");
        personDetailsJson.put("profile_path", "/test.jpg");
        personDetailsJson.put("known_for_department", "Acting");
        personDetailsJson.put("gender", 2);
        personDetailsJson.put("birthday", "1990-05-15");
        personDetailsJson.put("place_of_birth", "Los Angeles, USA");

        when(tmdbService.getPersonDetails(anyString(), anyString(), eq("2")))
                .thenReturn(personDetailsJson);

        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "Movie A", 10.0, 8.0, 100));
        items.add(new MovieOrTVShow("2", "Movie B", 20.0, 7.0, 200));
        models.PersonStats mockStats = new models.PersonStats(items);
        mockStats.setPersonDetails("Test Person", "/test.jpg", "Acting", 2, "1990-05-15", "Los Angeles, USA");

        HomeController localController = controllerWithSupervisorReply(mockStats);
        Http.Request request = Helpers.fakeRequest(GET, "/person/2/stats").build();
        Result result = localController.personStats("2", request).toCompletableFuture().join();
        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Test Person"));
        assertTrue(html.contains("Movie A"));
        assertTrue(html.contains("Movie B"));
    }
    private HomeController controllerWithSupervisorReply(Object replyObject) {
        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        // Use a classic ActorSystem from the application, not testKit's system
        ActorSystem classicSystem = application.injector().instanceOf(ActorSystem.class);

        ActorRef stubSupervisor = classicSystem.actorOf(
                org.apache.pekko.actor.Props.create(org.apache.pekko.actor.AbstractActor.class, () ->
                        new org.apache.pekko.actor.AbstractActor() {
                            @Override
                            public Receive createReceive() {
                                return receiveBuilder()
                                        .matchAny(msg -> getSender().tell(replyObject, getSelf()))
                                        .build();
                            }
                        })
        );
        Config config = mock(Config.class);
        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");
        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);
        Materializer materializer = application.injector().instanceOf(Materializer.class);
        return new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                mock(GenreService.class),
                tmdbService,
                mediaDetailsService,
                reviewsService,
                classicSystem,
                stubSupervisor,
                materializer,
                application.injector().instanceOf(MediaStreamService.class)
        );
    }
    /**
     * Tests the personStats action when API throws an exception.
     * Should still render the page gracefully with empty stats.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsActionWithApiFailure() throws Exception {
        when(tmdbService.getPersonCredits(anyString(), anyString(), eq("999")))
                .thenThrow(new RuntimeException("API failure"));

        Http.RequestBuilder requestBuilder = Helpers.fakeRequest(GET, "/person/999/stats");
        Http.Request request = requestBuilder.build();

        CompletionStage<Result> resultStage = controller.personStats("999", request);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Performance Metrics"));
    }

    /**
     * Tests the personStats action correctly renders person details (name, gender, birthday, etc.).
     * Verifies the profile information appears in the rendered HTML.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsRendersPersonDetails() throws Exception {
        // Mock credits
        com.fasterxml.jackson.databind.node.ObjectNode creditsJson = Json.newObject();
        creditsJson.set("cast", Json.newArray());
        creditsJson.set("crew", Json.newArray());

        when(tmdbService.getPersonCredits(anyString(), anyString(), eq("2")))
                .thenReturn(creditsJson);

        // Mock person details
        com.fasterxml.jackson.databind.node.ObjectNode personDetailsJson = Json.newObject();
        personDetailsJson.put("name", "Scarlett Johansson");
        personDetailsJson.put("profile_path", "/scarlett.jpg");
        personDetailsJson.put("known_for_department", "Acting");
        personDetailsJson.put("gender", 1);
        personDetailsJson.put("birthday", "1984-11-22");
        personDetailsJson.put("place_of_birth", "New York City, USA");

        when(tmdbService.getPersonDetails(anyString(), anyString(), eq("2")))
                .thenReturn(personDetailsJson);

        Http.Request request = Helpers.fakeRequest(GET, "/person/2/stats").build();

        models.PersonStats mockStats = new models.PersonStats(new ArrayList<>());
        mockStats.setPersonDetails("Scarlett Johansson", "/scarlett.jpg", "Acting", 1, "1984-11-22", "New York City, USA");

        HomeController localController = controllerWithSupervisorReply(mockStats);
        Result result = localController.personStats("2", request).toCompletableFuture().join();

        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Scarlett Johansson"));
        assertTrue(html.contains("Acting"));
        assertTrue(html.contains("Female"));
        assertTrue(html.contains("1984-11-22"));
        assertTrue(html.contains("New York City, USA"));
    }

    /**
     * Tests the personStats action correctly parses year from release_date for movies.
     * Verifies the year appears in the rendered page next to the title.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsRendersYearFromReleaseDate() throws Exception {
        // Mock credits with release_date
        JsonNode castItem = Json.newObject()
                .put("id", 1).put("title", "Inception")
                .put("popularity", 50.0).put("vote_average", 8.8).put("vote_count", 5000)
                .put("release_date", "2010-07-16");

        com.fasterxml.jackson.databind.node.ObjectNode creditsJson = Json.newObject();
        com.fasterxml.jackson.databind.node.ArrayNode castArray = Json.newArray();
        castArray.add(castItem);
        creditsJson.set("cast", castArray);
        creditsJson.set("crew", Json.newArray());

        when(tmdbService.getPersonCredits(anyString(), anyString(), eq("3")))
                .thenReturn(creditsJson);

        // Mock person details
        com.fasterxml.jackson.databind.node.ObjectNode personDetailsJson = Json.newObject();
        personDetailsJson.put("name", "Leonardo DiCaprio");
        personDetailsJson.put("profile_path", "/leo.jpg");
        personDetailsJson.put("known_for_department", "Acting");
        personDetailsJson.put("gender", 2);
        personDetailsJson.put("birthday", "1974-11-11");
        personDetailsJson.put("place_of_birth", "Los Angeles, USA");

        when(tmdbService.getPersonDetails(anyString(), anyString(), eq("3")))
                .thenReturn(personDetailsJson);

        Http.Request request = Helpers.fakeRequest(GET, "/person/3/stats").build();

        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "Inception", 50.0, 8.8, 5000, "2010"));
        models.PersonStats mockStats = new models.PersonStats(items);
        mockStats.setPersonDetails("Leonardo DiCaprio", "/leo.jpg", "Acting", 2, "1974-11-11", "Los Angeles, USA");

        HomeController localController = controllerWithSupervisorReply(mockStats);
        Result result = localController.personStats("3", request).toCompletableFuture().join();
        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Inception"));
        assertTrue(html.contains("2010"));
    }

    /**
     * Tests the personStats action correctly parses year from first_air_date for TV shows.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testPersonStatsRendersYearFromFirstAirDate() throws Exception {
        // Mock credits with first_air_date (TV show)
        JsonNode castItem = Json.newObject()
                .put("id", 1).put("name", "Breaking Bad")
                .put("popularity", 80.0).put("vote_average", 9.5).put("vote_count", 10000)
                .put("first_air_date", "2008-01-20");

        com.fasterxml.jackson.databind.node.ObjectNode creditsJson = Json.newObject();
        com.fasterxml.jackson.databind.node.ArrayNode castArray = Json.newArray();
        castArray.add(castItem);
        creditsJson.set("cast", castArray);
        creditsJson.set("crew", Json.newArray());

        when(tmdbService.getPersonCredits(anyString(), anyString(), eq("4")))
                .thenReturn(creditsJson);

        // Mock person details
        com.fasterxml.jackson.databind.node.ObjectNode personDetailsJson = Json.newObject();
        personDetailsJson.put("name", "Bryan Cranston");
        personDetailsJson.put("profile_path", "/bryan.jpg");
        personDetailsJson.put("known_for_department", "Acting");
        personDetailsJson.put("gender", 2);
        personDetailsJson.put("birthday", "1956-03-07");
        personDetailsJson.put("place_of_birth", "Canoga Park, USA");

        when(tmdbService.getPersonDetails(anyString(), anyString(), eq("4")))
                .thenReturn(personDetailsJson);

        Http.Request request = Helpers.fakeRequest(GET, "/person/4/stats").build();

        List<MovieOrTVShow> items = new ArrayList<>();
        items.add(new MovieOrTVShow("1", "Breaking Bad", 80.0, 9.5, 10000, "2008"));
        models.PersonStats mockStats = new models.PersonStats(items);
        mockStats.setPersonDetails("Bryan Cranston", "/bryan.jpg", "Acting", 2, "1956-03-07", "Canoga Park, USA");

        HomeController localController = controllerWithSupervisorReply(mockStats);
        Result result = localController.personStats("4", request).toCompletableFuture().join();
        assertEquals(OK, result.status());
        String html = contentAsString(result);
        assertTrue(html.contains("Breaking Bad"));
        assertTrue(html.contains("2008"));
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

    /**
     * Tests root redirect action.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testRedirectToFlicklytics() {
        Result result = controller.redirectToFlicklytics();
        assertEquals(SEE_OTHER, result.status());
        assertTrue(result.redirectLocation().isPresent());
        assertTrue(result.redirectLocation().get().contains("/flicklytics"));
    }

    /**
     * Tests that the WebSocket search endpoint returns a non-null WebSocket handler.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testWsEndpointNotNull() {
        assertNotNull(controller.ws());
    }

    /**
     * Tests private parseMediaItem branches via reflection.
     *
     * @author Syed Shahab Shah
     */
    @Test
    public void testParseMediaItemBranchVariantsViaReflection() throws Exception {
        Method m = HomeController.class.getDeclaredMethod("parseMediaItem", JsonNode.class);
        m.setAccessible(true);

        JsonNode movieNode = Json.newObject()
                .put("id", 1)
                .put("title", "Movie Title")
                .put("popularity", 1.5)
                .put("vote_average", 2.5)
                .put("vote_count", 3)
                .put("release_date", "2020-01-01");
        MovieOrTVShow movie = (MovieOrTVShow) m.invoke(controller, movieNode);
        assertEquals("Movie Title", movie.getTitle());
        assertEquals("2020", movie.getYear());

        JsonNode tvNode = Json.newObject()
                .put("name", "TV Name")
                .put("first_air_date", "2015-07-10");
        MovieOrTVShow tv = (MovieOrTVShow) m.invoke(controller, tvNode);
        assertEquals("TV Name", tv.getTitle());
        assertEquals("2015", tv.getYear());

        JsonNode emptyNode = Json.newObject();
        MovieOrTVShow fallback = (MovieOrTVShow) m.invoke(controller, emptyNode);
        assertEquals("Unknown", fallback.getTitle());
        assertEquals("", fallback.getYear());
        assertEquals("", fallback.getId());
    }

    /**
     * Tests constructor catch path when genre loading throws.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testConstructorHandlesGenreLoadFailure() throws Exception {
        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        GenreService throwingGenreService = mock(GenreService.class);
        Config config = mock(Config.class);
        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");
        doThrow(new RuntimeException("genre load failed"))
                .when(throwingGenreService)
                .loadGenres(anyString(), anyString(), anyMap(), anyMap());
        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);

        ActorSystem actorSystem = testKit.system().classicSystem();
        ActorRef supervisorActor = mock(ActorRef.class);
        Materializer materializer = application.injector().instanceOf(Materializer.class);

        HomeController localController = new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                throwingGenreService,
                tmdbService,
                mediaDetailsService,
                reviewsService,
                actorSystem,
                supervisorActor,
                materializer,
                application.injector().instanceOf(MediaStreamService.class)
        );

        Result result = localController.index(Helpers.fakeRequest(GET, "/flicklytics").build())
                .toCompletableFuture().join();
        assertEquals(OK, result.status());
    }

    /**
     * Covers generated controllers.routes wrappers (including nested javascript class).
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testGeneratedRoutesWrappersCoverage() {
        assertNotNull(new routes());
        assertNotNull(new routes.javascript());
        assertNotNull(routes.javascript.HomeController);
        assertNotNull(routes.javascript.Assets);
    }

    /**
     * Covers ReverseHomeController and ReverseAssets generated methods and prefix branches.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testReverseRoutesCoverage() {
        ReverseHomeController withSlash = new ReverseHomeController(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/";
            }
        });

        ReverseHomeController withoutSlash = new ReverseHomeController(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/api";
            }
        });

        assertTrue(withSlash.index().url().contains("flicklytics"));
        assertTrue(withoutSlash.index().url().contains("/api/flicklytics"));
        assertTrue(withSlash.movie(1L).url().contains("/movie/1"));
        assertTrue(withSlash.tv(2L).url().contains("/tv/2"));
        assertEquals("/", withSlash.redirectToFlicklytics().url());
        assertTrue(withSlash.ws().url().contains("ws/search"));
        assertTrue(withSlash.personStats("12").url().contains("/person/12/stats"));
        assertTrue(withSlash.reviews("movie", 10L).url().contains("/reviews/movie/10"));
        assertTrue(withSlash.globalDiversity("movie", 5).url().contains("global-diversity/movie/5"));
        assertTrue(withSlash.financialPerformance(9).url().contains("financial-performance/9"));

        ReverseAssets assetsWithSlash = new ReverseAssets(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/";
            }
        });

        ReverseAssets assetsWithoutSlash = new ReverseAssets(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/api";
            }
        });

        Call c1 = assetsWithSlash.versioned(new Assets.Asset("stylesheets/main.css"));
        Call c2 = assetsWithoutSlash.versioned(new Assets.Asset("stylesheets/main.css"));
        assertTrue(c1.url().contains("/flicklytics/assets/"));
        assertTrue(c2.url().contains("/api/flicklytics/assets/"));
    }

    /**
     * Covers controllers.javascript reverse route generated methods.
     *
     * @author Mahmoud Saghir
     */
    @Test
    public void testJavaScriptReverseRoutesCoverage() {
        controllers.javascript.ReverseHomeController jsWithSlash =
                new controllers.javascript.ReverseHomeController(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/";
                    }
                });

        controllers.javascript.ReverseHomeController jsWithoutSlash =
                new controllers.javascript.ReverseHomeController(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/api";
                    }
                });

        JavaScriptReverseRoute r1 = jsWithSlash.index();
        JavaScriptReverseRoute r2 = jsWithSlash.movie();
        JavaScriptReverseRoute r3 = jsWithSlash.tv();
        JavaScriptReverseRoute r4 = jsWithSlash.ws();
        JavaScriptReverseRoute r5 = jsWithSlash.redirectToFlicklytics();
        JavaScriptReverseRoute r6 = jsWithSlash.personStats();
        JavaScriptReverseRoute r7 = jsWithSlash.reviews();
        JavaScriptReverseRoute r8 = jsWithSlash.globalDiversity();
        JavaScriptReverseRoute r9 = jsWithSlash.financialPerformance();
        JavaScriptReverseRoute r10 = jsWithoutSlash.index();

        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        assertNotNull(r4);
        assertNotNull(r5);
        assertNotNull(r6);
        assertNotNull(r7);
        assertNotNull(r8);
        assertNotNull(r9);
        assertNotNull(r10);

        controllers.javascript.ReverseAssets jsAssetsWithSlash =
                new controllers.javascript.ReverseAssets(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/";
                    }
                });

        controllers.javascript.ReverseAssets jsAssetsWithoutSlash =
                new controllers.javascript.ReverseAssets(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/api";
                    }
                });

        assertNotNull(jsAssetsWithSlash.versioned());
        assertNotNull(jsAssetsWithoutSlash.versioned());
    }
    /**
     * Covers controllers_HomeController_movieWs10_route and invoker (LINE:29).
     * Tests both prefix branches (with and without slash) to initialize the
     * lazy val invoker which is the red block in the generated router.
     *
     * @author Zenghui WU
     */
    @Test
    public void testReverseHomeControllerMovieWsRoute() {
        ReverseHomeController withSlash = new ReverseHomeController(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/";
            }
        });

        ReverseHomeController withoutSlash = new ReverseHomeController(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/api";
            }
        });

        // Initializes controllers_HomeController_movieWs10_route lazy val
        Call movieWsWithSlash    = withSlash.movieWs();
        // Initializes the invoker lazy val (the red block) via defaultPrefix branch
        Call movieWsWithoutSlash = withoutSlash.movieWs();

        assertNotNull(movieWsWithSlash);
        assertNotNull(movieWsWithoutSlash);
        assertTrue(movieWsWithSlash.url().contains("ws/movie"));
        assertTrue(movieWsWithoutSlash.url().contains("/api"));
        assertTrue(movieWsWithoutSlash.url().contains("ws/movie"));
    }

    /**
     * Covers controllers_HomeController_tvWs11_route and invoker (LINE:30).
     * Tests both prefix branches (with and without slash) to initialize the
     * lazy val invoker which is the red block in the generated router.
     *
     * @author Zenghui WU
     */
    @Test
    public void testReverseHomeControllerTvWsRoute() {
        ReverseHomeController withSlash = new ReverseHomeController(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/";
            }
        });

        ReverseHomeController withoutSlash = new ReverseHomeController(new AbstractFunction0<String>() {
            @Override
            public String apply() {
                return "/api";
            }
        });

        // Initializes controllers_HomeController_tvWs11_route lazy val
        Call tvWsWithSlash    = withSlash.tvWs();
        // Initializes the invoker lazy val (the red block) via defaultPrefix branch
        Call tvWsWithoutSlash = withoutSlash.tvWs();

        assertNotNull(tvWsWithSlash);
        assertNotNull(tvWsWithoutSlash);
        assertTrue(tvWsWithSlash.url().contains("ws/tv"));
        assertTrue(tvWsWithoutSlash.url().contains("/api"));
        assertTrue(tvWsWithoutSlash.url().contains("ws/tv"));
    }

    /**
     * Covers the JavaScript reverse route wrappers for movieWs and tvWs.
     * Both withSlash and withoutSlash variants force the JS invoker
     * lazy vals to initialize, clearing the remaining red blocks.
     *
     * @author Zenghui WU
     */
    @Test
    public void testJavaScriptReverseMovieWsAndTvWsCoverage() {
        controllers.javascript.ReverseHomeController jsWithSlash =
                new controllers.javascript.ReverseHomeController(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/";
                    }
                });

        controllers.javascript.ReverseHomeController jsWithoutSlash =
                new controllers.javascript.ReverseHomeController(new AbstractFunction0<String>() {
                    @Override
                    public String apply() {
                        return "/api";
                    }
                });

        JavaScriptReverseRoute movieWsSlash    = jsWithSlash.movieWs();
        JavaScriptReverseRoute movieWsNoSlash  = jsWithoutSlash.movieWs();
        JavaScriptReverseRoute tvWsSlash       = jsWithSlash.tvWs();
        JavaScriptReverseRoute tvWsNoSlash     = jsWithoutSlash.tvWs();

        assertNotNull(movieWsSlash);
        assertNotNull(movieWsNoSlash);
        assertNotNull(tvWsSlash);
        assertNotNull(tvWsNoSlash);
    }

    /**
     * Tests that buildMediaFlow returns a non-null Flow for the "movie" media type.
     * <p>
     * The method under test (buildMediaFlow) wires together six components:
     * <ol>
     *   <li>An actorRef-backed Source (via Source.actorRef / preMaterialize)</li>
     *   <li>A child MediaDetailsActor created by asking the SupervisorActor</li>
     *   <li>An initial seed batch sent to that child actor (StartSession)</li>
     *   <li>A subscription to the live broadcast hub (mediaStreamService.liveSource)</li>
     *   <li>An inbound Sink that forwards browser messages as ChangeSearch commands</li>
     *   <li>A combined Flow assembled with Flow.fromSinkAndSource</li>
     * </ol>
     * Because the method is private it is invoked via reflection.  A stub
     * SupervisorActor is used so the ask in step 2 resolves immediately with a
     * real ActorRef, allowing every branch of the method to execute without
     * blocking or throwing.
     *
     * @author Zenghui WU
     */
    @Test
    public void testBuildMediaFlowReturnsNonNullFlowForMovie() throws Exception {
        // Build a controller whose supervisorActor stub replies with a real ActorRef
        // so the Patterns.ask inside buildMediaFlow resolves immediately.
        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        ActorSystem classicSystem = application.injector().instanceOf(ActorSystem.class);

        // The stub supervisor replies to any message with itself as the ActorRef,
        // satisfying the .thenApply(ActorRef.class::cast) cast inside buildMediaFlow.
        ActorRef stubSupervisor = classicSystem.actorOf(
                org.apache.pekko.actor.Props.create(
                        org.apache.pekko.actor.AbstractActor.class,
                        () -> new org.apache.pekko.actor.AbstractActor() {
                            @Override
                            public Receive createReceive() {
                                return receiveBuilder()
                                        .matchAny(msg -> getSender().tell(getSelf(), getSelf()))
                                        .build();
                            }
                        }
                )
        );

        Config config = mock(Config.class);
        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");
        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);

        Materializer materializer = application.injector().instanceOf(Materializer.class);

        HomeController localController = new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                mock(GenreService.class),
                tmdbService,
                mediaDetailsService,
                reviewsService,
                classicSystem,
                stubSupervisor,
                materializer,
                application.injector().instanceOf(MediaStreamService.class)
        );

        // Invoke the private buildMediaFlow method via reflection
        Method buildMediaFlow = HomeController.class
                .getDeclaredMethod("buildMediaFlow", String.class);
        buildMediaFlow.setAccessible(true);

        Object flow = buildMediaFlow.invoke(localController, "movie");

        assertNotNull("buildMediaFlow(\"movie\") must return a non-null Flow", flow);
    }

    /**
     * Tests that buildMediaFlow returns a non-null Flow for the "tv" media type.
     * <p>
     * Mirrors {@link #testBuildMediaFlowReturnsNonNullFlowForMovie()} but exercises
     * the "tv" branch so that any mediaType-specific logic (seed filtering, actor
     * naming, etc.) is also covered.
     *
     * @author Zenghui WU
     */
    @Test
    public void testBuildMediaFlowReturnsNonNullFlowForTv() throws Exception {
        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        ActorSystem classicSystem = application.injector().instanceOf(ActorSystem.class);

        ActorRef stubSupervisor = classicSystem.actorOf(
                org.apache.pekko.actor.Props.create(
                        org.apache.pekko.actor.AbstractActor.class,
                        () -> new org.apache.pekko.actor.AbstractActor() {
                            @Override
                            public Receive createReceive() {
                                return receiveBuilder()
                                        .matchAny(msg -> getSender().tell(getSelf(), getSelf()))
                                        .build();
                            }
                        }
                )
        );

        Config config = mock(Config.class);
        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");
        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);

        Materializer materializer = application.injector().instanceOf(Materializer.class);

        HomeController localController = new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                mock(GenreService.class),
                tmdbService,
                mediaDetailsService,
                reviewsService,
                classicSystem,
                stubSupervisor,
                materializer,
                application.injector().instanceOf(MediaStreamService.class)
        );

        Method buildMediaFlow = HomeController.class
                .getDeclaredMethod("buildMediaFlow", String.class);
        buildMediaFlow.setAccessible(true);

        Object flow = buildMediaFlow.invoke(localController, "tv");

        assertNotNull("buildMediaFlow(\"tv\") must return a non-null Flow", flow);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RED-LINE COVERAGE: financialPerformance — financialActor == null branch
    // Lines 249-254: when the controller is constructed via the 12-arg
    // convenience constructor, financialActor is null.  The method must fall
    // back to building FinancialPerformance directly and render the page.
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Covers the {@code financialActor == null} fallback branch (lines 249-254)
     * inside {@code financialPerformance()}.
     * <p>
     * The 12-argument convenience constructor passes {@code null} for the
     * {@code financialActor} parameter, so the {@code if (financialActor == null)}
     * guard is {@code true} and the method must construct a
     * {@link models.FinancialPerformance} directly from budget/revenue without
     * delegating to an actor.  The rendered page must still show
     * "Financial Performance".
     *
     * @author Zenghui WU
     */
    @Test
    public void testFinancialPerformanceFallbackWhenActorIsNull() throws Exception {
        // Arrange: return valid budget/revenue data from the service
        JsonNode mockDetails = Json.newObject()
                .put("budget", 200000L)
                .put("revenue", 800000L);

        when(tmdbService.getDetails(anyString(), anyString(), eq("movie"), eq(42L)))
                .thenReturn(mockDetails);

        // The setUp() controller is built via the 12-arg constructor → financialActor is null
        Http.Request request = Helpers.fakeRequest(GET, "/flicklytics/financial-performance/42").build();

        CompletionStage<Result> resultStage = controller.financialPerformance(request, 42);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Financial Performance"));
    }

    /**
     * Covers the {@code financialActor == null} fallback together with a zero
     * budget and zero revenue to exercise the {@code FinancialPerformance(0, 0)}
     * edge case in the same branch.
     *
     * @author Zenghui WU
     */
    @Test
    public void testFinancialPerformanceFallbackWithZeroBudgetAndRevenue() throws Exception {
        JsonNode mockDetails = Json.newObject()
                .put("budget", 0L)
                .put("revenue", 0L);

        when(tmdbService.getDetails(anyString(), anyString(), eq("movie"), eq(55L)))
                .thenReturn(mockDetails);

        Http.Request request = Helpers.fakeRequest(GET, "/flicklytics/financial-performance/55").build();

        CompletionStage<Result> resultStage = controller.financialPerformance(request, 55);
        Result result = resultStage.toCompletableFuture().join();

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains("Financial Performance"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RED-LINE COVERAGE: globalDiversity — catch block (lines 388-390)
    // Patterns.ask throws synchronously → catch fires → internalServerError
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Covers the {@code catch (Exception e)} block in {@code globalDiversity()}
     * (lines 388-390).
     * <p>
     * {@code Patterns.ask} is mocked to throw a {@link RuntimeException}
     * synchronously, which is caught inside the try-block and returned as an
     * {@code internalServerError("Failed to fetch TMDb data")}.
     *
     * @author Zenghui WU
     */
    @Test
    public void testGlobalDiversityCatchBlockOnAskFailure() throws Exception {
        Http.Request request = Helpers.fakeRequest(GET, "/flicklytics/global-diversity/movie/1").build();

        try (MockedStatic<Patterns> mockedPatterns =
                     Mockito.mockStatic(org.apache.pekko.pattern.Patterns.class)) {

            mockedPatterns.when(() ->
                    Patterns.ask(any(ActorRef.class), any(), any(java.time.Duration.class))
            ).thenThrow(new RuntimeException("actor unavailable"));

            CompletionStage<Result> resultStage = controller.globalDiversity(request, "movie", 1);
            Result result = resultStage.toCompletableFuture().join();

            assertEquals(INTERNAL_SERVER_ERROR, result.status());
            assertTrue(contentAsString(result).contains("Failed to fetch TMDb data"));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // COVERAGE: buildSeedItems — non-array results guard (line 653)
    // A cache entry whose "results" field is not an ArrayNode must be skipped.
    // (ConcurrentHashMap forbids null values, so we test the next early-continue
    //  branch: !resultArray.isArray())
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Covers the {@code if (!resultArray.isArray()) { continue; }} guard (line 653)
     * inside {@code buildSeedItems()}.
     * <p>
     * {@code ConcurrentHashMap} does not permit {@code null} values, so the null-node
     * guard on line 649 cannot be triggered via the map directly.  Instead, we inject
     * a cache entry whose {@code "results"} field is a plain {@code TextNode} rather
     * than an {@code ArrayNode}, which causes {@code resultArray.isArray()} to return
     * {@code false} and exercises the very next defensive {@code continue}.
     *
     * @author Zenghui WU
     */
    @Test
    public void testBuildSeedItemsSkipsNullCacheEntry() throws Exception {
        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        ActorSystem classicSystem = application.injector().instanceOf(ActorSystem.class);

        // Stub supervisor replies with itself so buildMediaFlow's ask resolves
        ActorRef stubSupervisor = classicSystem.actorOf(
                org.apache.pekko.actor.Props.create(
                        org.apache.pekko.actor.AbstractActor.class,
                        () -> new org.apache.pekko.actor.AbstractActor() {
                            @Override
                            public Receive createReceive() {
                                return receiveBuilder()
                                        .matchAny(msg -> getSender().tell(getSelf(), getSelf()))
                                        .build();
                            }
                        }
                )
        );

        Config config = mock(Config.class);
        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");
        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);

        Materializer materializer = application.injector().instanceOf(Materializer.class);

        HomeController localController = new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                mock(GenreService.class),
                tmdbService,
                mediaDetailsService,
                reviewsService,
                classicSystem,
                stubSupervisor,
                materializer,
                application.injector().instanceOf(MediaStreamService.class)
        );

        // Inject a cache entry whose "results" is not an array to cover the isArray() guard
        java.lang.reflect.Field cacheField =
                HomeController.class.getDeclaredField("searchCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, com.fasterxml.jackson.databind.JsonNode> cache =
                (java.util.Map<String, com.fasterxml.jackson.databind.JsonNode>)
                        cacheField.get(localController);
        // ConcurrentHashMap does not permit null values; use a node with a non-array
        // "results" field instead to cover the isArray() false-branch (line 653).
        com.fasterxml.jackson.databind.node.ObjectNode badNode = Json.newObject();
        badNode.put("results", "not-an-array");
        cache.put("bad-results-key", badNode);

        // buildMediaFlow → buildSeedItems must silently skip the bad entry
        Method buildMediaFlow = HomeController.class
                .getDeclaredMethod("buildMediaFlow", String.class);
        buildMediaFlow.setAccessible(true);

        Object flow = buildMediaFlow.invoke(localController, "movie");
        assertNotNull("buildMediaFlow must return non-null Flow when cache has non-array results node", flow);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // RED-LINE COVERAGE: buildSeedItems — !matchesMediaType continue (line 663)
    // An item whose media type does NOT match the requested type must be skipped.
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Covers the {@code if (!matchesMediaType(item, mediaType)) { continue; }}
     * branch (line 663) inside {@code buildSeedItems()}.
     * <p>
     * We populate the searchCache with a node that holds a "results" array
     * containing one "tv" item.  When {@code buildSeedItems} is called with
     * mediaType {@code "movie"} the item fails the type check and the
     * {@code continue} on line 663 is hit.  The returned seed list must be empty.
     *
     * @author Zenghui WU
     */
    @Test
    public void testBuildSeedItemsSkipsItemWithNonMatchingMediaType() throws Exception {
        Application application = new GuiceApplicationBuilder().build();
        Helpers.start(application);

        ActorSystem classicSystem = application.injector().instanceOf(ActorSystem.class);

        ActorRef stubSupervisor = classicSystem.actorOf(
                org.apache.pekko.actor.Props.create(
                        org.apache.pekko.actor.AbstractActor.class,
                        () -> new org.apache.pekko.actor.AbstractActor() {
                            @Override
                            public Receive createReceive() {
                                return receiveBuilder()
                                        .matchAny(msg -> getSender().tell(getSelf(), getSelf()))
                                        .build();
                            }
                        }
                )
        );

        Config config = mock(Config.class);
        when(config.getString("tmdb.api.key")).thenReturn("fake-key");
        when(config.getString("tmdb.api.url")).thenReturn("fake-url");
        when(tmdbService.loadTargetLanguageConstant(anyString(), anyString())).thenReturn(10);

        Materializer materializer = application.injector().instanceOf(Materializer.class);

        HomeController localController = new HomeController(
                application.injector().instanceOf(FormFactory.class),
                application.injector().instanceOf(MessagesApi.class),
                application.injector().instanceOf(ClassLoaderExecutionContext.class),
                config,
                mock(GenreService.class),
                tmdbService,
                mediaDetailsService,
                reviewsService,
                classicSystem,
                stubSupervisor,
                materializer,
                application.injector().instanceOf(MediaStreamService.class)
        );

        // Build a cache entry whose single result item has type "tv"
        com.fasterxml.jackson.databind.node.ObjectNode tvItem = Json.newObject();
        tvItem.put("type", "tv");
        tvItem.put("link", "/tv/999");
        tvItem.put("title", "A TV Show");

        com.fasterxml.jackson.databind.node.ObjectNode cacheNode = Json.newObject();
        cacheNode.set("results", Json.newArray().add(tvItem));

        java.lang.reflect.Field cacheField =
                HomeController.class.getDeclaredField("searchCache");
        cacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, com.fasterxml.jackson.databind.JsonNode> cache =
                (java.util.Map<String, com.fasterxml.jackson.databind.JsonNode>)
                        cacheField.get(localController);
        cache.put("tv:999", cacheNode);

        // Invoke buildSeedItems("movie", "") — the tv item must be skipped (line 663)
        Method buildSeedItems = HomeController.class
                .getDeclaredMethod("buildSeedItems", String.class, String.class);
        buildSeedItems.setAccessible(true);

        @SuppressWarnings("unchecked")
        java.util.List<com.fasterxml.jackson.databind.node.ObjectNode> seeds =
                (java.util.List<com.fasterxml.jackson.databind.node.ObjectNode>)
                        buildSeedItems.invoke(localController, "movie", "");

        assertTrue("Seed list must be empty when no item matches the requested media type",
                seeds.isEmpty());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // YELLOW-LINE COVERAGE: parseMediaItem — dateStr null branch (line 347)
    // The ternary  (dateStr != null && dateStr.length() >= 4) must be exercised
    // with a non-null dateStr that is shorter than 4 characters so the false
    // branch returns an empty String instead of a substring.
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Covers the false branch of the {@code dateStr.length() >= 4} guard on
     * line 347 inside {@code parseMediaItem()}.
     * <p>
     * When {@code release_date} exists but is shorter than 4 characters (e.g.
     * {@code "20"}) the conditional must evaluate to {@code false} and
     * {@code year} must be set to the empty string rather than a substring.
     *
     * @author Zenghui WU
     */
    @Test
    public void testParseMediaItemWithShortDateReturnsEmptyYear() throws Exception {
        Method m = HomeController.class.getDeclaredMethod("parseMediaItem",
                com.fasterxml.jackson.databind.JsonNode.class);
        m.setAccessible(true);

        // release_date present but only 2 characters long — length() >= 4 is false
        com.fasterxml.jackson.databind.JsonNode node = Json.newObject()
                .put("id", 7)
                .put("title", "Short Date Movie")
                .put("popularity", 1.0)
                .put("vote_average", 5.0)
                .put("vote_count", 10)
                .put("release_date", "20");   // length 2 < 4

        MovieOrTVShow result = (MovieOrTVShow) m.invoke(controller, node);

        assertEquals("Short Date Movie", result.getTitle());
        assertEquals("Year must be empty when dateStr is shorter than 4 chars",
                "", result.getYear());
    }

    /**
     * Covers the branch where both {@code release_date} and
     * {@code first_air_date} are absent, so {@code dateStr} is {@code ""}
     * (empty string) and the {@code dateStr.length() >= 4} guard is false,
     * returning an empty year. This is a complementary path to
     * {@link #testParseMediaItemWithShortDateReturnsEmptyYear()}.
     *
     * @author Zenghui WU
     */
    @Test
    public void testParseMediaItemWithEmptyDateStringReturnsEmptyYear() throws Exception {
        Method m = HomeController.class.getDeclaredMethod("parseMediaItem",
                com.fasterxml.jackson.databind.JsonNode.class);
        m.setAccessible(true);

        // Neither release_date nor first_air_date present → dateStr = ""
        com.fasterxml.jackson.databind.JsonNode node = Json.newObject()
                .put("id", 8)
                .put("title", "No Date Movie")
                .put("popularity", 2.0)
                .put("vote_average", 6.0)
                .put("vote_count", 20);

        MovieOrTVShow result = (MovieOrTVShow) m.invoke(controller, node);

        assertEquals("No Date Movie", result.getTitle());
        assertEquals("", result.getYear());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // YELLOW-LINE COVERAGE: matchesMediaType — link-based fallback (lines 685-690)
    // When item.path("type") is blank the method falls through to the link check.
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Covers the link-based fallback branch in {@code matchesMediaType()}
     * (lines 689-690) where {@code item.path("type")} is blank so the method
     * must fall through to {@code link.startsWith("/" + mediaType + "/")}.
     * <p>
     * Case A — link matches: expects {@code true}.
     *
     * @author Zenghui WU
     */
    @Test
    public void testMatchesMediaTypeUsesLinkWhenTypeFieldIsBlank_Match() throws Exception {
        Method m = HomeController.class.getDeclaredMethod(
                "matchesMediaType",
                com.fasterxml.jackson.databind.JsonNode.class, String.class);
        m.setAccessible(true);

        // No "type" field → type.isBlank() is true → fall through to link check
        com.fasterxml.jackson.databind.node.ObjectNode item = Json.newObject();
        item.put("link", "/movie/123");   // link starts with "/movie/"

        boolean result = (boolean) m.invoke(controller, item, "movie");
        assertTrue("Item with link '/movie/123' must match mediaType 'movie'", result);
    }

    /**
     * Covers the link-based fallback branch in {@code matchesMediaType()}
     * (lines 689-690) where the link does NOT start with the requested prefix.
     * <p>
     * Case B — link does not match: expects {@code false}.
     *
     * @author Zenghui WU
     */
    @Test
    public void testMatchesMediaTypeUsesLinkWhenTypeFieldIsBlank_NoMatch() throws Exception {
        Method m = HomeController.class.getDeclaredMethod(
                "matchesMediaType",
                com.fasterxml.jackson.databind.JsonNode.class, String.class);
        m.setAccessible(true);

        com.fasterxml.jackson.databind.node.ObjectNode item = Json.newObject();
        item.put("link", "/tv/456");   // link starts with "/tv/" not "/movie/"

        boolean result = (boolean) m.invoke(controller, item, "movie");
        assertFalse("Item with link '/tv/456' must NOT match mediaType 'movie'", result);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // YELLOW-LINE COVERAGE: matchesQuery — partial short-circuit (line 704)
    // All three sub-expressions must be exercised to saturate the || chain.
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Covers the {@code name.contains(q)} arm of the {@code return} on line 704
     * inside {@code matchesQuery()}, where {@code title} does NOT match but
     * {@code name} DOES match.
     *
     * @author Zenghui WU
     */
    @Test
    public void testMatchesQueryMatchesOnNameField() throws Exception {
        Method m = HomeController.class.getDeclaredMethod(
                "matchesQuery",
                com.fasterxml.jackson.databind.JsonNode.class, String.class);
        m.setAccessible(true);

        com.fasterxml.jackson.databind.node.ObjectNode item = Json.newObject();
        item.put("title", "unrelated");
        item.put("name", "Breaking Bad");
        item.put("overview", "unrelated overview");

        boolean result = (boolean) m.invoke(controller, item, "breaking");
        assertTrue("matchesQuery must return true when the 'name' field contains the query", result);
    }

    /**
     * Covers the {@code overview.contains(q)} arm of the {@code return} on
     * line 704 inside {@code matchesQuery()}, where neither {@code title} nor
     * {@code name} match but {@code overview} DOES match.
     *
     * @author Zenghui WU
     */
    @Test
    public void testMatchesQueryMatchesOnOverviewField() throws Exception {
        Method m = HomeController.class.getDeclaredMethod(
                "matchesQuery",
                com.fasterxml.jackson.databind.JsonNode.class, String.class);
        m.setAccessible(true);

        com.fasterxml.jackson.databind.node.ObjectNode item = Json.newObject();
        item.put("title", "some title");
        item.put("name", "some name");
        item.put("overview", "A thrilling story about espionage");

        boolean result = (boolean) m.invoke(controller, item, "espionage");
        assertTrue("matchesQuery must return true when the 'overview' field contains the query", result);
    }

    /**
     * Covers the all-false path of line 704 in {@code matchesQuery()} where
     * none of the three fields match, so the method returns {@code false}.
     *
     * @author Zenghui WU
     */
    @Test
    public void testMatchesQueryReturnsFalseWhenNoFieldMatches() throws Exception {
        Method m = HomeController.class.getDeclaredMethod(
                "matchesQuery",
                com.fasterxml.jackson.databind.JsonNode.class, String.class);
        m.setAccessible(true);

        com.fasterxml.jackson.databind.node.ObjectNode item = Json.newObject();
        item.put("title", "Movie Title");
        item.put("name", "Movie Name");
        item.put("overview", "Movie overview text");

        boolean result = (boolean) m.invoke(controller, item, "xyz_no_match");
        assertFalse("matchesQuery must return false when no field contains the query", result);
    }
    /**
     * Tests that the movie WebSocket endpoint returns a non-null WebSocket handler.
     *
     * Covers:
     *   public WebSocket movieWs() {
     *       return WebSocket.Json.accept(req -> buildMediaFlow("movie"));
     *   }
     */
    @Test
    public void testMovieWsEndpointNotNull() {
        assertNotNull(controller.movieWs());
    }

    /**
     * Tests that the TV WebSocket endpoint returns a non-null WebSocket handler.
     *
     * Covers:
     *   public WebSocket tvWs() {
     *       return WebSocket.Json.accept(req -> buildMediaFlow("tv"));
     *   }
     */
    @Test
    public void testTvWsEndpointNotNull() {
        assertNotNull(controller.tvWs());
    }


}