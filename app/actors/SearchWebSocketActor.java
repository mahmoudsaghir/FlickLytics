package actors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.Cancellable;
import org.apache.pekko.actor.Props;
import services.TmdbService;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private int emptyTicks = 0;
    private int currentPage = 1;

    public SearchWebSocketActor(ActorRef out, TmdbService tmdbService, String apiUrl, String token) {
        this.out = out;
        this.tmdbService = tmdbService;
        this.apiUrl = apiUrl;
        this.token = token;
    }

    public static Props props(ActorRef out, TmdbService tmdbService, String apiUrl, String token) {
        return Props.create(SearchWebSocketActor.class, () -> new SearchWebSocketActor(out, tmdbService, apiUrl, token));
    }

    @Override
    public void preStart() {
        getContext().getSystem().log().info("SearchWebSocketActor started with API URL: {}", apiUrl);
    }

    @Override
    public void postStop() {
        if (tickTask != null && !tickTask.isCancelled()) {
            tickTask.cancel();
        }
        getContext().getSystem().log().info("SearchWebSocketActor stopped");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("tick", t -> handleTick())
                .match(String.class, this::handleIncomingMessage)
                .build();
    }

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
            emptyTicks = 0;

            List<JsonNode> results = tmdbService.searchNow(apiUrl, token, currentQuery, currentCategory, currentPage);

            results.stream().limit(10).forEach(r -> {
                int id = r.get("id").asInt();
                sentIds.add(id);
                out.tell(r.toString(), getSelf());
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
            e.printStackTrace();
            getContext().getSystem().log().error("Failed to parse incoming message: {}", message, e);
        }
    }

    private void handleTick() {
        getContext().getSystem().log().info("Sending periodic updates...");
        if (currentQuery.isEmpty()) {
            return;
        }

        List<JsonNode> results = tmdbService.searchNow(apiUrl, token, currentQuery, currentCategory, currentPage);

        boolean foundNew = false;
        for (JsonNode r : results) {
            int id = r.get("id").asInt();

            if (!sentIds.contains(id)) {
                foundNew = true;
                sentIds.add(id);
                out.tell(r.toString(), getSelf());
            }
        }

        if (!foundNew) {
            currentPage++;
            getContext().getSystem().log().info("No new results, moving to page {}", currentPage);
            out.tell("{\"type\":\"heartbeat\"}", getSelf());
        } else {
            emptyTicks = 0;
        }

        // Do NOT cancel tickTask automatically, keep polling for new data
        // Optionally, log if currentPage exceeds TMDB limit
        if (currentPage > 500) {
            getContext().getSystem().log().info("Reached max page limit, continuing polling...");
        }
    }
}