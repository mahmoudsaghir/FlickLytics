package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import forms.SearchForm;
import models.Utils;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.concurrent.ClassLoaderExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    private final FormFactory formFactory;
    private final MessagesApi messagesApi;
    private final ClassLoaderExecutionContext clExecutionContext;
    private final String tmdbToken;
    private final String apiUrl;
    private final int targetLanguageConstant;

    // cache for search results
    private static final Map<String, JsonNode> searchCache = new ConcurrentHashMap<>();

    // caches for genres to avoid repeated API calls
    private final Map<Integer, String> movieGenres = new ConcurrentHashMap<>();
    private final Map<Integer, String> tvGenres = new ConcurrentHashMap<>();

    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi,
                          ClassLoaderExecutionContext clExecutionContext, Config config) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
        this.clExecutionContext = clExecutionContext;
        this.tmdbToken = config.getString("tmdb.api.key");
        this.apiUrl = config.getString("tmdb.api.url");

        // load genres at startup to populate the genre maps
        loadGenres();
        // load the target language constant at startup
        this.targetLanguageConstant = loadTargetLanguageConstant();
    }

    /**
     * An action that redirects to the Flicklytics page.
     *
     * @return A redirect result to the Flicklytics page
     * @author Mahmoud Saghir
     */
    public Result redirectToFlicklytics() {
        return redirect("/flicklytics");
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
                        String searchUrl = this.apiUrl + "search/";
                        if (category.equals("movie"))
                            searchUrl += "movie?query=" + query;
                        else if (category.equals("tv"))
                            searchUrl += "tv?query=" + query;
                        else if (category.equals("person"))
                            searchUrl += "person?query=" + query;

                        JsonNode rootNode = Utils.sendGetRequest(searchUrl, this.tmdbToken);

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
                                .collect(Collectors.toList());

                        ArrayNode filteredResults = Json.newArray();
                        filteredResultsList.stream().forEach(filteredResults::add);

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
                        // Fetch Details API (Original Overview)
                        String detailsUrl = this.apiUrl + category + "/" + id;
                        JsonNode detailsRoot = Utils.sendGetRequest(detailsUrl, this.tmdbToken);

                        String originalOverview = detailsRoot.path("overview").asText("");

                        // Extract mediaName after originalOverview
                        String mediaName;
                        if (category.equals("movie")) {
                            mediaName = detailsRoot.path("title").asText("");
                        } else {
                            mediaName = detailsRoot.path("name").asText("");
                        }

                        int originalLength = originalOverview.length();

                        // Fetch Translations API
                        String translationUrl = this.apiUrl + category + "/" + id + "/translations";
                        JsonNode translationRoot = Utils.sendGetRequest(translationUrl, this.tmdbToken);

                        ArrayNode translationsArray = (ArrayNode) translationRoot.get("translations");

                        // Compute Translation Density
                        double translationDensity = (double) translationsArray.size() / this.targetLanguageConstant;

                        // Compute Localization Index
                        double localizationIndex = StreamSupport.stream(translationsArray.spliterator(), false)
                                .map(translationNode -> translationNode.path("data").path("overview").asText(""))
                                .filter(overview -> !overview.isEmpty())
                                .mapToDouble(overview -> (double) overview.length() / originalLength)
                                .average()
                                .orElse(0.0);

                        // Return Metrics
                        ObjectNode response = Json.newObject();

                        response.put("translation_density", Math.round(translationDensity * 100.0) / 100.0);
                        response.put("localization_index", Math.round(localizationIndex * 100.0) / 100.0);
                        // Add media_name to response
                        response.put("media_name", mediaName);

                        return response.toString();

                    } catch (Exception e) {
                        e.printStackTrace();
                        return "{\"error\":\"Failed to fetch TMDB data\"}";
                    }

                }, clExecutionContext.current())
                .thenApply(resultsJson -> {
                    JsonNode node = Json.parse(resultsJson);
                    // Extract mediaName from node
                    String mediaName = node.path("media_name").asText("");

                    double translationDensity = Math.round(node.path("translation_density").asDouble(0.0) * 100.0) / 100.0;
                    double localizationIndex = Math.round(node.path("localization_index").asDouble(0.0) * 100.0) / 100.0;

                    // Pass mediaName to the view
                    return ok(views.html.globalDiversity.render(
                            category,
                            id,
                            mediaName,
                            translationDensity,
                            localizationIndex,
                            request,
                            messages
                    ));
                });
    }

    /**
     * Loads genres for movies and TV shows from TMDb API and populates the genre maps.
     *
     * @author Mahmoud Saghir
     */
    private void loadGenres() {
        try {
            loadGenreCategory("movie", movieGenres);
            loadGenreCategory("tv", tvGenres);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param category The category of genres to load (e.g., "movie" or "tv")
     * @param genreMap The map to populate with genre ID and name pairs
     * @throws Exception if there is an error during the API call or processing the response
     * @author Mahmoud Saghir
     */
    private void loadGenreCategory(String category, Map<Integer, String> genreMap) throws Exception {
        String genreUrl = this.apiUrl + "genre/" + category + "/list";
        JsonNode root = Utils.sendGetRequest(genreUrl, this.tmdbToken);

        ArrayNode genresArray = (ArrayNode) root.get("genres");

        StreamSupport.stream(genresArray.spliterator(), false)
                .forEach(node -> {
                    int id = node.path("id").asInt();
                    String name = node.path("name").asText();
                    genreMap.put(id, name);
                });
    }

    /**
     * Loads the number of translations from TMDB collection translations API.
     *
     * @return number of translations (size of translations list)
     * @author Mahmoud Saghir
     */
    private int loadTargetLanguageConstant() {
        try {
            String urlStr = this.apiUrl + "collection/10/translations";
            JsonNode root = Utils.sendGetRequest(urlStr, this.tmdbToken);
            JsonNode translations = root.path("translations");

            if (translations.isArray()) {
                return translations.size();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback value to avoid division by zero
        return 1;
    }
}
