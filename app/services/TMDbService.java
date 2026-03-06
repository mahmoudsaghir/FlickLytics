package services;

import com.fasterxml.jackson.databind.JsonNode;
import models.MovieOrTVShow;
import models.PersonStats;
import play.libs.ws.*;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service class for interacting with the TMDb (The Movie Database) REST API.
 * This service handles all API communication for searching movies, TV shows, and persons,
 * as well as retrieving detailed statistics about persons' known works.
 *
 * The class uses the Play Framework's WSClient for making asynchronous HTTP requests
 * and Java 8+ Streams API for data processing. All operations return CompletionStage
 * for non-blocking, reactive programming.
 *
 * @author Syed Shahab Shah
 */
@Singleton
public class TMDbService {
    private final WSClient ws;
    /** The TMDb API key used for authentication */
    private final String apiKey = "7060bd173385f50b066b7117b8791033";
    /** The base URL for all TMDb API endpoints */
    private final String baseUrl = "https://api.themoviedb.org/3";

    /**
     * Constructs a TMDbService with dependency injection.
     * This constructor is called automatically by Play Framework's Guice injector.
     *
     * @param ws The WSClient for making HTTP requests (injected by Play)
     * @author Syed Shahab Shah
     */
    @Inject
    public TMDbService(WSClient ws) {
        this.ws = ws;
    }

    /**
     * Searches for movies, TV shows, or persons using the TMDb API.
     * Returns a maximum of 10 results matching the query in the specified category.
     *
     * This method is asynchronous and non-blocking, using Java's CompletionStage
     * for reactive programming. The results are processed using Java 8+ Streams API.
     *
     * @param query The search query string (e.g., "Star Wars", "Breaking Bad", "Tom Hanks")
     * @param category The category to search in: "movie", "tv", or "person"
     * @return A CompletionStage containing a list of up to 10 MovieOrTVShow results
     * @author Syed Shahab Shah
     */
    public CompletionStage<List<MovieOrTVShow>> searchAnything(String query, String category) {
        return ws.url(baseUrl + "/search/" + category)
                .addQueryParameter("api_key", apiKey)
                .addQueryParameter("query", query)
                .get()
                .thenApply(response -> {
                    JsonNode resultsNode = response.asJson().path("results");
                    return StreamSupport.stream(resultsNode.spliterator(), false)
                            .map(node -> new MovieOrTVShow(
                                    node.path("id").asText(),
                                    node.has("title") ? node.path("title").asText() : node.path("name").asText("Unknown"),
                                    node.path("popularity").asDouble(0.0),
                                    node.path("vote_average").asDouble(0.0),
                                    node.path("vote_count").asInt(0)
                            ))
                            .collect(Collectors.toList());
                });
    }

    /**
     * Retrieves a person's "known for" items from the TMDb API.
     * This is used to display the works a person is credited for, limited to the 50 most recent.
     * Also calculates aggregate statistics for these items.
     *
     * This method is asynchronous and non-blocking, using Java's CompletionStage
     * for reactive programming. The results are processed using Java 8+ Streams API.
     *
     * @param personId The TMDb ID of the person (as a String)
     * @return A CompletionStage containing a PersonStats object with up to 50 items and their statistics
     * @author Syed Shahab Shah
     */
    public CompletionStage<PersonStats> getPersonStats(String personId) {
        return ws.url(baseUrl + "/person/" + personId + "/combined_credits")
                .addQueryParameter("api_key", apiKey)
                .get()
                .thenApply(response -> {
                    JsonNode cast = response.asJson().path("cast");
                    List<MovieOrTVShow> items = StreamSupport.stream(cast.spliterator(), false)
                            .map(node -> new MovieOrTVShow(
                                    node.path("id").asText(),
                                    node.has("title") ? node.path("title").asText() : node.path("name").asText("Unknown"),
                                    node.path("popularity").asDouble(0.0),
                                    node.path("vote_average").asDouble(0.0),
                                    node.path("vote_count").asInt(0)
                            ))
                            .collect(Collectors.toList());
                    return new PersonStats(items);
                });
    }
}