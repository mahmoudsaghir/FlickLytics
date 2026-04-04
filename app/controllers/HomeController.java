package controllers;

import actors.GlobalDiversityActor;
import actors.SearchWebSocketActor;
import actors.SupervisorActor;
import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import forms.SearchForm;
import models.FinancialPerformance;
import models.GlobalDiversityResult;
import models.MovieOrTVShow;
import models.PersonStats;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Props;
import org.apache.pekko.pattern.Patterns;
import org.apache.pekko.stream.Materializer;
import org.apache.pekko.stream.OverflowStrategy;
import org.webjars.play.WebJarsUtil;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.libs.streams.ActorFlow;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;
import services.GenreService;
import services.MediaDetailsService;
import services.ReviewsService;
import services.TmdbService;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

/**
 * Main controller for the FlickLytics web application.
 * Handles HTTP requests and responses for the search functionality and person statistics pages.
 * All controller actions return CompletionStage for asynchronous, non-blocking processing.
 * This is the single controller for the entire application as per requirements,
 * with business logic delegated to service and model classes.
 *
 * @author Syed Shahab Shah
 * @author Mahmoud Saghir
 * @author Zenghui WU
 * @author Tasmia Naomi
 * @author Charles Wang
 */
public class HomeController extends Controller {

    private final FormFactory formFactory;
    private final MessagesApi messagesApi;
    private final ClassLoaderExecutionContext clExecutionContext;
    private final String tmdbToken;
    private final String apiUrl;
    private final int targetLanguageConstant;

    // Service for media details and readability
    private final MediaDetailsService mediaDetailsService;
    // Service for reviews and sentiment analysis
    private final ReviewsService reviewsService;

    // cache for search results
    private final Map<String, JsonNode> searchCache = new ConcurrentHashMap<>();

    // caches for genres to avoid repeated API calls
    private final Map<Integer, String> movieGenres = new ConcurrentHashMap<>();
    private final Map<Integer, String> tvGenres = new ConcurrentHashMap<>();

    private final TmdbService tmdbService;

    private final ActorSystem actorSystem;
    private final ActorRef supervisorActor;

    private final Materializer materializer;

    @Inject
    WebJarsUtil webJarsUtil;

    /**
     * Constructs the HomeController with all required dependencies.
     * Dependencies are injected by Play Framework's Guice injector.
     *
     * @param formFactory         Play's form factory for handling form data
     * @param messagesApi         Play's messages API for internationalization
     * @param clExecutionContext  Play's ClassLoaderExecutionContext for async operations
     * @param config              The application configuration
     * @param genreService        The Genre service for loading genre maps
     * @param tmdbService         The TMDb service for API communication
     * @param mediaDetailsService The Media Details service for readability calculations
     * @param reviewsService      The Reviews service for sentiment analysis
     * @param actorSystem         The Pekko Actor System for concurrency
     * @author Mahmoud Saghir
     */
    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi,
                          ClassLoaderExecutionContext clExecutionContext, Config config,
                          GenreService genreService, TmdbService tmdbService,
                          MediaDetailsService mediaDetailsService, ReviewsService reviewsService,
                          ActorSystem actorSystem, @Named("supervisorActor") ActorRef supervisorActor,
                          Materializer materializer) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
        this.clExecutionContext = clExecutionContext;
        this.tmdbToken = config.getString("tmdb.api.key");
        this.apiUrl = config.getString("tmdb.api.url");
        this.tmdbService = tmdbService;
        this.mediaDetailsService = mediaDetailsService;
        this.reviewsService = reviewsService;
        this.materializer = materializer;

        // load genres at startup to populate the genre maps
        try {
            genreService.loadGenres(this.apiUrl, this.tmdbToken, movieGenres, tvGenres);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // load the target language constant at startup
        this.targetLanguageConstant = this.tmdbService.loadTargetLanguageConstant(apiUrl, tmdbToken);

        this.actorSystem = actorSystem;
        this.supervisorActor = supervisorActor;
    }

    /**
     * An action that redirects to the Flicklytics page.
     *
     * @return A redirect result to the Flicklytics page
     * @author Mahmoud Saghir
     */
    public Result redirectToFlicklytics() {
        return redirect(routes.HomeController.index());
    }

    /**
     * An action that renders the index page with an empty form.
     *
     * @param request The HTTP request
     * @return A promise to render the index page
     * @author Mahmoud Saghir
     */
    public CompletionStage<Result> index(Http.Request request) {
        Messages messages = messagesApi.preferred(request);
        Form<SearchForm> form = formFactory.form(SearchForm.class);
        return CompletableFuture.completedFuture(
                ok(views.html.index.render(form, request, messages, null, webJarsUtil))
                        .withNewSession()
        );
    }

    /**
     * Retrieves movie details from the TMDB service, extracts the
     * budget and revenue values, creates a FinancialPerformance object, and
     * renders the financial performance view.
     * This action is asynchronous and non-blocking.
     * Error handling is included to report API failures.
     *
     * @param request The HTTP request
     * @param id      The unique identifier of the movie
     * @return a promise to render containing the rendered financial performance page,
     * or an internal server error if the movie data cannot be retrieved
     * @author Charles Wang
     * @author Tasmia Naomi
     */

    public CompletionStage<Result> financialPerformance(Http.Request request, Integer id) {
        Messages messages = messagesApi.preferred(request);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return tmdbService.getDetails(apiUrl, tmdbToken, "movie", id.longValue());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, clExecutionContext.current()).thenApply(details -> {

            if (details == null) {
                return internalServerError("Failed to fetch movie financial data");
            }

            long budget = details.path("budget").asLong(0);
            long revenue = details.path("revenue").asLong(0);

            FinancialPerformance fp = new FinancialPerformance(budget, revenue);

            return ok(views.html.financialPerformance.render(fp, request, messages, webJarsUtil));
        });
    }

    /**
     * Handles GET requests to display a person's "known for" items and statistics.
     * Retrieves data asynchronously from the TMDb API and displays comprehensive statistics.
     * This action is asynchronous and non-blocking, returning a CompletionStage.
     * Error handling is included to gracefully report any API failures.
     *
     * @param id      The TMDb person ID (passed from the URL)
     * @param request The HTTP request object (injected by Play)
     * @return A CompletionStage containing an HTTP result rendering the person stats page
     * @author Syed Shahab Shah
     */
    public CompletionStage<Result> personStats(String id, Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        JsonNode json = tmdbService.getPersonCredits(apiUrl, tmdbToken, id);
                        JsonNode personDetails = tmdbService.getPersonDetails(apiUrl, tmdbToken, id);
                        List<MovieOrTVShow> allItems = new java.util.ArrayList<>();

                        // Parse cast credits
                        JsonNode cast = json.get("cast");
                        if (cast != null && cast.isArray()) {
                            StreamSupport.stream(cast.spliterator(), false)
                                    .map(this::parseMediaItem)
                                    .forEach(allItems::add);
                        }

                        // Parse crew credits
                        JsonNode crew = json.get("crew");
                        if (crew != null && crew.isArray()) {
                            StreamSupport.stream(crew.spliterator(), false)
                                    .map(this::parseMediaItem)
                                    .forEach(allItems::add);
                        }

                        return new SupervisorActor.ComputePersonStats(
                                allItems,
                                personDetails.path("name").asText(null),
                                personDetails.path("profile_path").asText(null),
                                personDetails.path("known_for_department").asText(null),
                                personDetails.path("gender").asInt(0),
                                personDetails.path("birthday").asText(null),
                                personDetails.path("place_of_birth").asText(null)
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }, clExecutionContext.current())
                .thenCompose(command -> {
                    if (command == null) {
                        return CompletableFuture.completedFuture(new PersonStats(null));
                    }
                    return Patterns.ask(supervisorActor, command, Duration.ofSeconds(3))
                            .thenApply(resultObj -> (PersonStats) resultObj)
                            .exceptionally(ex -> new PersonStats(null));
                })
                .thenApply(stats -> {
                    Messages messages = messagesApi.preferred(request);
                    return ok(views.html.personStats.render(stats, request, messages, webJarsUtil));
                });
    }

    /**
     * Parses a single media item JSON node into a MovieOrTVShow object.
     * Handles both movie and TV show formats from the TMDb API.
     *
     * @param node The JSON node representing a media item
     * @return A MovieOrTVShow object
     * @author Syed Shahab Shah
     */
    private MovieOrTVShow parseMediaItem(JsonNode node) {
        String itemId = node.has("id") ? node.get("id").asText() : "";
        String title = node.has("title") ? node.get("title").asText() :
                node.has("name") ? node.get("name").asText() : "Unknown";
        double popularity = node.has("popularity") ? node.get("popularity").asDouble() : 0.0;
        double voteAverage = node.has("vote_average") ? node.get("vote_average").asDouble() : 0.0;
        int voteCount = node.has("vote_count") ? node.get("vote_count").asInt() : 0;

        // Extract year from release_date (movies) or first_air_date (TV shows)
        String dateStr = node.has("release_date") ? node.get("release_date").asText("") :
                node.has("first_air_date") ? node.get("first_air_date").asText("") : "";
        String year = (dateStr != null && dateStr.length() >= 4) ? dateStr.substring(0, 4) : "";

        return new MovieOrTVShow(itemId, title, popularity, voteAverage, voteCount, year);
    }

    /**
     * An action that renders the Global Diversity page for a given TMDb ID and category.
     *
     * @param category The category (movie, tv)
     * @param id       The TMDb ID of the movie or TV show
     * @return A result rendering the Global Diversity page with computed metrics
     * @author Mahmoud Saghir
     */
    public CompletionStage<Result> globalDiversity(Http.Request request, String category, Integer id) {
        Messages messages = messagesApi.preferred(request);

        try {
            Duration timeout = Duration.ofSeconds(3);
            CompletionStage<Object> resultFuture = Patterns.ask(
                    supervisorActor,
                    new GlobalDiversityActor.ComputeDiversity(
                            category,
                            id.longValue(),
                            targetLanguageConstant
                    ),
                    timeout
            );

            return resultFuture.thenApply(resultObj -> {
                GlobalDiversityResult globalDiversityResult = (GlobalDiversityResult) resultObj;
                return ok(views.html.globalDiversity.render(
                        category,
                        id,
                        globalDiversityResult.mediaName,
                        globalDiversityResult.translationDensity,
                        globalDiversityResult.localizationIndex,
                        request,
                        messages,
                        webJarsUtil
                ));
            });
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(internalServerError("Failed to fetch TMDb data"));
        }
    }

    /**
     * An action that renders the movie details page with readability scores.
     *
     * @param id      The TMDb ID of the movie
     * @param request The HTTP request object
     * @return A CompletionStage rendering the movie details page with readability scores
     * @author Zenghui WU
     */
    public CompletionStage<Result> movie(Long id, Http.Request request) {
        return fetchAndRender("movie", id, request);
    }

    /**
     * An action that renders the TV show details page with readability scores.
     *
     * @param id      The TMDb ID of the TV show
     * @param request The HTTP request object
     * @return A CompletionStage rendering the TV show details page with readability scores
     * @author Zenghui WU
     */
    public CompletionStage<Result> tv(Long id, Http.Request request) {
        return fetchAndRender("tv", id, request);
    }

    /**
     * Fetches details for a movie or TV show and renders the details page.
     *
     * @param type    The type ("movie" or "tv")
     * @param id      The TMDb ID
     * @param request The HTTP request
     * @return A CompletionStage rendering the details page
     * @author Zenghui WU
     */
    private CompletionStage<Result> fetchAndRender(String type, Long id, Http.Request request) {
        Messages messages = messagesApi.preferred(request);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return mediaDetailsService.getDetailsWithReadability(apiUrl, tmdbToken, type, id);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, clExecutionContext.current()).thenApply(result -> {
            if (result == null || result.details == null) {
                return internalServerError("Failed to fetch details");
            }

            return ok(views.html.details.render(
                    type,
                    result.details,
                    result.overview,
                    result.scores,
                    request,
                    messages,
                    webJarsUtil
            ));
        });
    }

    /**
     * An action that renders the reviews page with sentiment analysis for a given media item.
     * Fetches up to 50 reviews from the TMDb API and performs sentiment analysis on each.
     * Displays aggregated sentiment statistics and individual review details.
     *
     * @param type    The media type ("movie" or "tv")
     * @param id      The TMDb media ID
     * @param request The HTTP request object
     * @return A CompletionStage rendering the reviews page with sentiment analysis
     * @author Tasmia Naomi
     */
    public CompletionStage<Result> reviews(String type, Long id, Http.Request request) {
        Messages messages = messagesApi.preferred(request);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return reviewsService.getReviewsWithSentiment(apiUrl, tmdbToken, type, id);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, clExecutionContext.current()).thenApply(reviewsSummary -> {
            if (reviewsSummary == null) {
                return internalServerError("Failed to fetch reviews");
            }

            return ok(views.html.reviews.render(
                    type,
                    id,
                    reviewsSummary,
                    request,
                    messages,
                    webJarsUtil
            ));
        });
    }

    /**
     * An action that handles the search form submission and renders the search results page.
     *
     * @return A WebSocket that handles search queries and sends search results back to the client
     * @author Mahmoud Saghir
     */
    public WebSocket ws() {
        return WebSocket.Text.accept(request ->
                ActorFlow.actorRef(
                        out -> Props.create(SearchWebSocketActor.class, out, tmdbService, apiUrl, tmdbToken, movieGenres, tvGenres),
                        16,
                        OverflowStrategy.dropBuffer(),
                        actorSystem,
                        materializer
                )
        );
    }
}