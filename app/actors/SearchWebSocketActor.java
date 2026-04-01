package actors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Cancellable;
import org.apache.pekko.actor.Props;
import services.TmdbService;

import java.time.Duration;
import java.util.*;

/**
 * Actor responsible for handling search WebSocket requests.
 * Periodically sends periodic updates to the client.
 *
 * @author Mahmoud Saghir
 */
public class SearchWebSocketActor extends AbstractActor {

    private final ActorRef out;
    private final TmdbService tmdbService;
    private final String apiUrl;
    private final String token;

    private final Set<Integer> sentIds = new HashSet<>();
    private String currentQuery = "";
    private String currentCategory = "";

    private Cancellable tickTask;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private int currentPage = 1;

    private final List<String> searchHistory = new ArrayList<>();

    private final Map<Integer, String> movieGenres;
    private final Map<Integer, String> tvGenres;

    /**
     * Constructor for SearchWebSocketActor.
     *
     * @param out         ActorRef to send messages back to the client
     * @param tmdbService TMDbService instance
     * @param apiUrl      TMDb API base URL
     * @param token       Bearer token for authorization
     * @author Mahmoud Saghir
     */
    public SearchWebSocketActor(ActorRef out, TmdbService tmdbService, String apiUrl, String token,
                                Map<Integer, String> movieGenres, Map<Integer, String> tvGenres) {
        this.out = out;
        this.tmdbService = tmdbService;
        this.apiUrl = apiUrl;
        this.token = token;
        this.movieGenres = movieGenres;
        this.tvGenres = tvGenres;
    }

    /**
     * Creates Props for SearchWebSocketActor.
     *
     * @param out         ActorRef to send messages back to the client
     * @param tmdbService TMDbService instance
     * @param apiUrl      TMDb API base URL
     * @param token       Bearer token for authorization
     * @param movieGenres Map of movie genre IDs to names
     * @param tvGenres    Map of TV genre IDs to names
     * @return Props for creating SearchWebSocketActor
     * @author Mahmoud Saghir
     */
    public static Props props(ActorRef out, TmdbService tmdbService, String apiUrl, String token,
                              Map<Integer, String> movieGenres, Map<Integer, String> tvGenres) {
        return Props.create(SearchWebSocketActor.class,
                () -> new SearchWebSocketActor(out, tmdbService, apiUrl, token, movieGenres, tvGenres));
    }

    /**
     * Logs when the actor is started and stops when it is stopped.
     *
     * @author Mahmoud Saghir
     */
    @Override
    public void preStart() {
        getContext().getSystem().log().info("SearchWebSocketActor started with API URL: {}", apiUrl);
    }

    /**
     * Cancels the periodic task when the actor is stopped.
     *
     * @author Mahmoud Saghir
     */
    @Override
    public void postStop() {
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
        }
        getContext().getSystem().log().info("SearchWebSocketActor stopped");
    }

    /**
     * Handles incoming messages and sends periodic updates to the client.
     *
     * @return Receive builder for handling incoming messages and periodic updates
     * @author Mahmoud Saghir
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("tick", t -> handleTick())
                .match(String.class, this::handleIncomingMessage)
                .build();
    }

    /**
     * Handles incoming search requests from the client.
     *
     * @param message JSON string containing the search query and category
     * @author Mahmoud Saghir
     */
    private void handleIncomingMessage(String message) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String query = json.has("query") && !json.get("query").isNull() ? json.get("query").asText() : "";
            String category = json.has("category") && !json.get("category").isNull() ? json.get("category").asText() : "";

            getContext().getSystem().log().info("Received search query: {} for category: {}", query, category);

            currentQuery = query;
            currentCategory = category;
            sentIds.clear();
            currentPage = 1;

            // Notify UI to reset results
            out.tell("{\"type\":\"reset\"}", getSelf());

            searchHistory.add(currentQuery + ":" + currentCategory);
            if (searchHistory.size() > 10) {
                searchHistory.remove(0);
            }

            ObjectNode responseNode;
            try {
                responseNode = tmdbService.searchNow(apiUrl, token, currentQuery, currentCategory, currentPage);
            } catch (Exception e) {
                getContext().getSystem().log().error("TMDb API failed", e);
                out.tell("{\"type\":\"error\",\"message\":\"API failure\"}", getSelf());
                return;
            }

            int totalResults = responseNode.path("total_results").asInt(0);
            JsonNode resultsArray = responseNode.path("results");

            // Send total_results
            out.tell("{\"type\":\"total_results\",\"total_results\":" + totalResults + "}", getSelf());

            resultsArray.forEach(r -> {
                int id = r.get("id").asInt();
                if (!sentIds.contains(id)) {
                    sentIds.add(id);
                    // Map genre_ids to genre_names
                    if (r.has("genre_ids")) {
                        ArrayNode ids = (ArrayNode) r.get("genre_ids");
                        ArrayNode names = objectMapper.createArrayNode();
                        for (JsonNode idNode : ids) {
                            int gid = idNode.asInt();
                            String name = "movie".equals(currentCategory)
                                    ? movieGenres.getOrDefault(gid, "Unknown")
                                    : tvGenres.getOrDefault(gid, "Unknown");
                            names.add(name);
                        }
                        ((ObjectNode) r).set("genre_names", names);
                    }
                    out.tell(r.toString(), getSelf());
                }
            });

            if (tickTask != null && !tickTask.isCancelled()) {
                tickTask.cancel();
            }
            tickTask = getContext().getSystem().scheduler().scheduleWithFixedDelay(
                    Duration.ofSeconds(5),
                    Duration.ofSeconds(5),
                    getSelf(),
                    "tick",
                    getContext().getSystem().dispatcher(),
                    getSelf()
            );

        } catch (Exception e) {
            getContext().getSystem().log().error("Failed to parse incoming message: {}", message, e);
            out.tell("{\"type\":\"error\",\"message\":\"Invalid request\"}", getSelf());
        }
    }

    /**
     * Periodically sends periodic updates to the client.
     *
     * @author Mahmoud Saghir
     */
    private void handleTick() {
        getContext().getSystem().log().info("Sending periodic updates...");
        if (currentQuery.isEmpty()) {
            return;
        }

        ObjectNode responseNode;
        try {
            responseNode = tmdbService.searchNow(apiUrl, token, currentQuery, currentCategory, currentPage);
        } catch (Exception e) {
            getContext().getSystem().log().error("TMDb API failed", e);
            out.tell("{\"type\":\"error\",\"message\":\"API failure\"}", getSelf());
            throw e;
        }

        int totalResults = responseNode.path("total_results").asInt(0);
        JsonNode resultsArray = responseNode.path("results");

        // Send total_results
        out.tell("{\"type\":\"total_results\",\"total_results\":" + totalResults + "}", getSelf());

        // Send new results
        boolean foundNew = false;

        List<JsonNode> newResults = new ArrayList<>();
        resultsArray.forEach(r -> {
            int id = r.get("id").asInt();
            if (!sentIds.contains(id)) {
                sentIds.add(id);
                // Map genre_ids to genre_names
                if (r.has("genre_ids")) {
                    ArrayNode ids = (ArrayNode) r.get("genre_ids");
                    ArrayNode names = objectMapper.createArrayNode();
                    for (JsonNode idNode : ids) {
                        int gid = idNode.asInt();
                        String name = "movie".equals(currentCategory)
                                ? movieGenres.getOrDefault(gid, "Unknown")
                                : tvGenres.getOrDefault(gid, "Unknown");
                        names.add(name);
                    }
                    ((ObjectNode) r).set("genre_names", names);
                }
                newResults.add(r);
            }
        });

        if (!newResults.isEmpty()) {
            foundNew = true;
            newResults.forEach(r -> out.tell(r.toString(), getSelf()));
        }

        if (!foundNew) {
            currentPage++;
            getContext().getSystem().log().info("No new results, moving to page {}", currentPage);
            out.tell("{\"type\":\"heartbeat\"}", getSelf());
        }
    }
}