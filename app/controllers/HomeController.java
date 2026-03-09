package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import forms.SearchForm;
import models.GlobalDiversityResult;
import models.MovieOrTVShow;
import models.PersonStats;
import models.Readability;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.GenreService;
import services.GlobalDiversityService;
import services.TmdbService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import services.MediaDetailsService;

/**
 * Main controller for the FlickLytics web application.
 * Handles HTTP requests and responses for the search functionality and person statistics pages.
 * All controller actions return CompletionStage for asynchronous, non-blocking processing.
 *
 * This is the single controller for the entire application as per requirements,
 * with business logic delegated to service and model classes.
 *
 * @author Syed Shahab Shah
 * @author Mahmoud Saghir
 * @author Zenghui WU
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

    // cache for search results
    private final Map<String, JsonNode> searchCache = new ConcurrentHashMap<>();

    // caches for genres to avoid repeated API calls
    private final Map<Integer, String> movieGenres = new ConcurrentHashMap<>();
    private final Map<Integer, String> tvGenres = new ConcurrentHashMap<>();

    private final GlobalDiversityService globalDiversityService;
    private final TmdbService tmdbService;

    /**
     * Constructs the HomeController with all required dependencies.
     * Dependencies are injected by Play Framework's Guice injector.
     *
     * @param formFactory Play's form factory for handling form data
     * @param messagesApi Play's messages API for internationalization
     * @param clExecutionContext Play's ClassLoaderExecutionContext for async operations
     * @param config The application configuration
     * @param globalDiversityService The Global Diversity service
     * @param genreService The Genre service for loading genre maps
     * @param tmdbService The TMDb service for API communication
     * @author Mahmoud Saghir
     */
    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi,
                          ClassLoaderExecutionContext clExecutionContext, Config config,
                          GlobalDiversityService globalDiversityService, GenreService genreService,
                          TmdbService tmdbService, MediaDetailsService mediaDetailsService) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
        this.clExecutionContext = clExecutionContext;
        this.tmdbToken = config.getString("tmdb.api.key");
        this.apiUrl = config.getString("tmdb.api.url");
        this.globalDiversityService = globalDiversityService;
        this.tmdbService = tmdbService;
        this.mediaDetailsService = mediaDetailsService;

        // load genres at startup to populate the genre maps
        try {
            genreService.loadGenres(this.apiUrl, this.tmdbToken, movieGenres, tvGenres);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // load the target language constant at startup
        this.targetLanguageConstant = this.tmdbService.loadTargetLanguageConstant(apiUrl, tmdbToken);
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
                ok(views.html.index.render(form, request, messages, null))
                        .withNewSession()
        );
    }

    /**
     * An action that renders financial data.
     *
     * @param request The HTTP request
     * @param id The unique identifier of the movie
     * @return A promise to render the information for the financial performance page, or an error if fetching fails
     * @author Charles Wang
     */

    public Result financialPerformance(Http.Request request, Integer id) {
        Messages messages = messagesApi.preferred(request);

        try {
            // Fetch movie details only
            JsonNode details = tmdbService.getDetails(apiUrl, tmdbToken, "movie", id.longValue());

            return ok(views.html.financialPerformance.render(details, request, messages));
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError("Failed to fetch movie financial data");
        }
    }

    /**
     * Handles GET requests to display a person's "known for" items and statistics.
     * Retrieves data asynchronously from the TMDb API and displays comprehensive statistics.
     *
     * This action is asynchronous and non-blocking, returning a CompletionStage.
     * Error handling is included to gracefully report any API failures.
     *
     * @param id The TMDb person ID (passed from the URL)
     * @param request The HTTP request object (injected by Play)
     * @return A CompletionStage containing an HTTP result rendering the person stats page
     * @author Syed Shahab Shah
     */
    public CompletionStage<Result> personStats(String id, Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        JsonNode json = tmdbService.getPersonCredits(apiUrl, tmdbToken, id);
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

                        return new PersonStats(allItems);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new PersonStats(null);
                    }
                }, clExecutionContext.current())
                .thenApply(stats -> {
                    Messages messages = messagesApi.preferred(request);
                    return ok(views.html.personStats.render(stats, request, messages));
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

        return new MovieOrTVShow(itemId, title, popularity, voteAverage, voteCount);
    }

    /***
     * An action that handles the search form submission, performs the TMDb API call,
     * processes the results, and renders the index page with search results.
     * @author Mahmoud Saghir
     * @param request The HTTP request
     * @return A promise to render the index page with search results
     */
    public CompletionStage<Result> search(Http.Request request) {
        Messages messages = messagesApi.preferred(request);
        Form<SearchForm> form = formFactory.form(SearchForm.class).bindFromRequest(request);

        if (form.hasErrors()) {
            // Render the page with the submitted form to show errors
            return CompletableFuture.completedFuture(ok(views.html.index.render(form, request, messages, null)));
        }

        String query = form.get().query;
        String category = form.get().category;

        // Run API call asynchronously using supplyAsync and ClassLoaderExecutionContext
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        JsonNode rootNode = tmdbService.search(apiUrl, tmdbToken, query, category);

                        ArrayNode resultsArray = (ArrayNode) rootNode.get("results");

                        List<ObjectNode> filteredResultsList = StreamSupport.stream(resultsArray.spliterator(), false)
                                .map(item -> {
                                    ObjectNode filteredItem = Json.newObject();
                                    if (category.equals("movie")) {

                                        filteredItem.put("id", item.path("id").asInt(0));
                                        filteredItem.put("title", item.path("title").asText(""));
                                        filteredItem.put("link", "/movie/" + item.path("id").asText(""));
                                        filteredItem.put("language", item.path("original_language").asText(""));

                                        // convert genre IDs to genre names using cached movieGenres map
                                        ArrayNode genreNames = Json.newArray();
                                        for (JsonNode genreIdNode : item.path("genre_ids")) {
                                            int genreId = genreIdNode.asInt();
                                            String genreName = movieGenres.getOrDefault(genreId, "Unknown");
                                            genreNames.add(genreName);
                                        }
                                        filteredItem.set("genres", genreNames);
                                        filteredItem.put("release_date", item.path("release_date").asText(""));
                                        filteredItem.put("popularity", item.path("popularity").asDouble(0.0));
                                        filteredItem.put("vote_average", item.path("vote_average").asDouble(0.0));
                                    } else if (category.equals("tv")) {
                                        filteredItem.put("id", item.path("id").asInt(0));
                                        filteredItem.put("name", item.path("name").asText(""));
                                        filteredItem.put("link", "/tv/" + item.path("id").asText(""));
                                        filteredItem.put("language", item.path("original_language").asText(""));
                                        // convert genre IDs to genre names using cached tvGenres map
                                        ArrayNode genreNames = Json.newArray();
                                        for (JsonNode genreIdNode : item.path("genre_ids")) {
                                            int genreId = genreIdNode.asInt();
                                            String genreName = tvGenres.getOrDefault(genreId, "Unknown");
                                            genreNames.add(genreName);
                                        }
                                        filteredItem.set("genres", genreNames);
                                        filteredItem.put("first_air_date", item.path("first_air_date").asText(""));
                                        filteredItem.put("popularity", item.path("popularity").asDouble(0.0));
                                        filteredItem.put("vote_average", item.path("vote_average").asDouble(0.0));
                                    } else {
                                        filteredItem.put("id", item.path("id").asInt(0));
                                        filteredItem.put("name", item.path("name").asText(""));
                                        filteredItem.put("photo_link", item.path("profile_path").isNull() || item.path("profile_path").asText().isEmpty() ? "" : "https://image.tmdb.org/t/p/w500" + item.path("profile_path").asText(""));
                                        filteredItem.put("gender", item.path("gender").asInt(0));
                                        filteredItem.put("popularity", item.path("popularity").asDouble(0.0));
                                        filteredItem.put("known_for_department", item.path("known_for_department").asText(""));
                                        ArrayNode knownForArray = Json.newArray();
                                        JsonNode knownFor = item.path("known_for");
                                        if (knownFor.isArray()) {
                                            for (JsonNode knownForItem : knownFor) {
                                                ObjectNode knownForFiltered = Json.newObject();
                                                knownForFiltered.put("title", knownForItem.has("title") ? knownForItem.path("title").asText("") : knownForItem.path("name").asText(""));
                                                knownForFiltered.put("link", knownForItem.has("title") ? "/movie/" + knownForItem.path("id").asText("") : "/tv/" + knownForItem.path("id").asText(""));
                                                knownForFiltered.put("media_type", knownForItem.path("media_type").asText(""));
                                                knownForArray.add(knownForFiltered);
                                            }
                                        }
                                        filteredItem.set("known_for", knownForArray);
                                    }
                                    return filteredItem;
                                })
                                .limit(10)
                                .toList();

                        ArrayNode filteredResults = Json.newArray();
                        filteredResultsList.forEach(filteredResults::add);

                        // Use the original total_results from TMDb
                        int totalResults = rootNode.path("total_results").asInt(0);

                        // Build the filtered response object
                        ObjectNode filteredResponse = Json.newObject();
                        filteredResponse.put("total_results", totalResults);
                        filteredResponse.set("results", filteredResults);

                        // Return the JSON string for thenApply
                        return filteredResponse.toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "{\"error\":\"Failed to fetch TMDb data\"}";
                    }
                }, clExecutionContext.current())
                .thenApply(resultsJson -> {
                    // Parse new search result
                    JsonNode newSearchNode = Json.parse(resultsJson);

                    // Build a wrapper object containing metadata and results
                    ObjectNode searchWrapper = Json.newObject();
                    searchWrapper.put("query", query);
                    searchWrapper.put("category", category);
                    searchWrapper.put("total_results", newSearchNode.path("total_results").asInt(0));
                    searchWrapper.set("results", newSearchNode.path("results"));

                    // Generate a unique ID for this search
                    String searchId = UUID.randomUUID().toString();

                    // Store a full result in a server-side cache
                    searchCache.put(searchId, searchWrapper);

                    // Get previous search IDs from the session (comma separated)
                    String oldIds = request.session().get("searchHistory").orElse("");
                    String updatedIds;

                    if (oldIds.isEmpty()) {
                        updatedIds = searchId;
                    } else {
                        updatedIds = searchId + "," + oldIds;
                    }

                    // Keep at most 10 search queries in history
                    String[] ids = updatedIds.split(",");
                    if (ids.length > 10) {
                        updatedIds = String.join(",", Arrays.copyOf(ids, 10));
                    }

                    // Build ArrayNode to send it to view
                    ArrayNode historyArray = Json.newArray();
                    Stream.of(updatedIds.split(","))
                            .map(searchCache::get)
                            .forEach(historyArray::add);

                    return ok(views.html.index.render(form, request, messages, historyArray.toString()))
                            .addingToSession(request, "searchHistory", updatedIds);
                });
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
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        JsonNode detailsRoot = tmdbService.getDetails(apiUrl, tmdbToken, category, id.longValue());
                        JsonNode translationRoot = tmdbService.getTranslations(apiUrl, tmdbToken, category, id.longValue());

                        return globalDiversityService.compute(
                                category,
                                detailsRoot,
                                translationRoot,
                                this.targetLanguageConstant
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "{\"error\":\"Failed to fetch TMDB data\"}";
                    }

                }, clExecutionContext.current())
                .thenApply(result -> {
                    GlobalDiversityResult globalDiversityResult = (GlobalDiversityResult) result;

                    return ok(views.html.globalDiversity.render(
                            category,
                            id,
                            globalDiversityResult.mediaName,
                            globalDiversityResult.translationDensity,
                            globalDiversityResult.localizationIndex,
                            request,
                            messages
                    ));
                });
    }
    /**
     * An action that renders the movie details page with readability scores.
     *
     * @author Zenghui WU
     */
    public CompletionStage<Result> movie(Long id, Http.Request request) {
        return fetchAndRender("movie", id, request);
    }

    /**
     * An action that renders the TV show details page with readability scores.
     *
     * @author Zenghui WU
     */
    public CompletionStage<Result> tv(Long id, Http.Request request) {
        return fetchAndRender("tv", id, request);
    }

    /**
     * Fetches details for a movie or TV show and renders the details page.
     *
     * @param type The type ("movie" or "tv")
     * @param id The TMDb ID
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
                    messages
            ));
        });
    }




}