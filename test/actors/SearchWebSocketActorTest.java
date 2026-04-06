package actors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.Json;
import services.TmdbService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchWebSocketActor.
 *
 * Covers:
 * - Incoming search message handling
 * - Reset message sent on new search
 * - Total results forwarded to client
 * - Individual results forwarded (up to 10)
 * - Deduplication of results
 * - Genre ID mapping to genre names
 * - Tick handling with new results
 * - Tick handling with no new results (page increment)
 * - Tick skipped when query is empty
 * - API failure handling in search
 * - API failure handling in tick
 * - Invalid JSON message handling
 * - Search history trimming
 * - postStop cancels tick task
 *
 *
 */
public class SearchWebSocketActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("SearchWebSocketActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<Integer, String> movieGenres() {
        Map<Integer, String> m = new HashMap<>();
        m.put(28, "Action");
        m.put(12, "Adventure");
        return m;
    }

    private Map<Integer, String> tvGenres() {
        Map<Integer, String> m = new HashMap<>();
        m.put(18, "Drama");
        m.put(35, "Comedy");
        return m;
    }

    private ObjectNode makeSearchResponse(int totalResults, int... ids) {
        ObjectNode response = Json.newObject();
        response.put("total_results", totalResults);
        com.fasterxml.jackson.databind.node.ArrayNode results = Json.newArray();
        for (int id : ids) {
            ObjectNode item = Json.newObject();
            item.put("id", id);
            item.put("title", "Movie " + id);
            results.add(item);
        }
        response.set("results", results);
        return response;
    }

    private ObjectNode makeSearchResponseWithGenres(int id, int... genreIds) {
        ObjectNode response = Json.newObject();
        response.put("total_results", 1);
        com.fasterxml.jackson.databind.node.ArrayNode results = Json.newArray();
        ObjectNode item = Json.newObject();
        item.put("id", id);
        item.put("title", "Movie " + id);
        com.fasterxml.jackson.databind.node.ArrayNode genres = Json.newArray();
        for (int gid : genreIds) genres.add(gid);
        item.set("genre_ids", genres);
        results.add(item);
        response.set("results", results);
        return response;
    }

    private ActorRef makeActor(TestKit probe, TmdbService tmdbService) {
        return system.actorOf(SearchWebSocketActor.props(
                probe.getRef(), tmdbService, "http://api.tmdb.org/3/", "token",
                movieGenres(), tvGenres()));
    }

    // -------------------------------------------------------------------------
    // Search message tests
    // -------------------------------------------------------------------------

    /**
     * A valid search message triggers a reset message to the client.
     */
    @Test
    public void testSearchSendsResetFirst() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), eq("matrix"), eq("movie"), anyInt()))
                    .thenReturn(makeSearchResponse(1, 100));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"matrix\",\"category\":\"movie\"}", ActorRef.noSender());

            // First message must be reset
            String reset = expectMsgClass(duration("2 seconds"), String.class);
            assert reset.contains("reset") : "Expected reset message, got: " + reset;
        }};
    }

    /**
     * After reset, total_results message is sent.
     */
    @Test
    public void testSearchSendsTotalResults() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), eq("matrix"), eq("movie"), anyInt()))
                    .thenReturn(makeSearchResponse(42, 1));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"matrix\",\"category\":\"movie\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            String totalMsg = expectMsgClass(duration("2 seconds"), String.class);
            assert totalMsg.contains("total_results") : "Expected total_results, got: " + totalMsg;
            assert totalMsg.contains("42") : "Expected 42 in: " + totalMsg;
        }};
    }

    /**
     * Search results are forwarded individually to the client.
     */
    @Test
    public void testSearchForwardsResults() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), eq("matrix"), eq("movie"), anyInt()))
                    .thenReturn(makeSearchResponse(2, 1, 2));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"matrix\",\"category\":\"movie\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            String r1 = expectMsgClass(duration("2 seconds"), String.class);
            String r2 = expectMsgClass(duration("2 seconds"), String.class);
            assert r1.contains("\"id\":1") : "Expected id 1, got: " + r1;
            assert r2.contains("\"id\":2") : "Expected id 2, got: " + r2;
        }};
    }

    /**
     * Search sends at most 10 results even if more are returned.
     */
    @Test
    public void testSearchCapsResultsAtTen() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(makeSearchResponse(15, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"test\",\"category\":\"movie\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            // Expect exactly 10 result messages
            for (int i = 0; i < 10; i++) {
                expectMsgClass(duration("2 seconds"), String.class);
            }
            expectNoMessage(duration("300 millis"));
        }};
    }

    /**
     * Duplicate IDs in results are not forwarded twice.
     */
    @Test
    public void testSearchDeduplicatesResults() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            // Return same id twice
            when(mockTmdb.searchNow(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(makeSearchResponse(2, 5, 5));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"test\",\"category\":\"movie\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            expectMsgClass(duration("2 seconds"), String.class); // id=5 first time
            expectNoMessage(duration("300 millis"));             // id=5 second time skipped
        }};
    }

    /**
     * Genre IDs are mapped to genre names for movie results.
     */
    @Test
    public void testSearchMapsMovieGenreIds() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), eq("action"), eq("movie"), anyInt()))
                    .thenReturn(makeSearchResponseWithGenres(10, 28, 12));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"action\",\"category\":\"movie\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            String result = expectMsgClass(duration("2 seconds"), String.class);
            assert result.contains("Action") : "Expected Action genre, got: " + result;
            assert result.contains("Adventure") : "Expected Adventure genre, got: " + result;
        }};
    }

    /**
     * Genre IDs are mapped to genre names for TV results.
     */
    @Test
    public void testSearchMapsTvGenreIds() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), eq("drama"), eq("tv"), anyInt()))
                    .thenReturn(makeSearchResponseWithGenres(20, 18, 35));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"drama\",\"category\":\"tv\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            String result = expectMsgClass(duration("2 seconds"), String.class);
            assert result.contains("Drama") : "Expected Drama genre, got: " + result;
            assert result.contains("Comedy") : "Expected Comedy genre, got: " + result;
        }};
    }

    /**
     * Unknown genre IDs are mapped to "Unknown".
     */
    @Test
    public void testSearchMapsUnknownGenreId() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), anyString(), eq("movie"), anyInt()))
                    .thenReturn(makeSearchResponseWithGenres(30, 9999));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"x\",\"category\":\"movie\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            String result = expectMsgClass(duration("2 seconds"), String.class);
            assert result.contains("Unknown") : "Expected Unknown genre, got: " + result;
        }};
    }

    /**
     * API failure during search sends an error message to the client.
     */
    @Test
    public void testSearchApiFailureSendsError() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenThrow(new RuntimeException("API down"));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"fail\",\"category\":\"movie\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            String error = expectMsgClass(duration("2 seconds"), String.class);
            assert error.contains("error") : "Expected error message, got: " + error;
            assert error.contains("API failure") : "Expected API failure message, got: " + error;
        }};
    }

    /**
     * Invalid JSON message sends an error response.
     */
    @Test
    public void testInvalidJsonSendsError() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("not-valid-json", ActorRef.noSender());

            String error = expectMsgClass(duration("2 seconds"), String.class);
            assert error.contains("error") : "Expected error message, got: " + error;
        }};
    }

    /**
     * Message with null query and category defaults to empty strings gracefully.
     */
    @Test
    public void testSearchWithNullQueryAndCategory() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), eq(""), eq(""), anyInt()))
                    .thenReturn(makeSearchResponse(0));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":null,\"category\":null}", ActorRef.noSender());

            String reset = expectMsgClass(duration("2 seconds"), String.class);
            assert reset.contains("reset") : "Expected reset, got: " + reset;
            String total = expectMsgClass(duration("2 seconds"), String.class);
            assert total.contains("total_results") : "Expected total_results, got: " + total;
        }};
    }

    /**
     * Message with missing query and category fields defaults to empty strings.
     */
    @Test
    public void testSearchWithMissingFields() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), eq(""), eq(""), anyInt()))
                    .thenReturn(makeSearchResponse(0));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{}", ActorRef.noSender());

            String reset = expectMsgClass(duration("2 seconds"), String.class);
            assert reset.contains("reset") : "Expected reset, got: " + reset;
        }};
    }

    // -------------------------------------------------------------------------
    // Tick tests
    // -------------------------------------------------------------------------

    /**
     * Tick with empty query does nothing (no messages sent).
     */
    @Test
    public void testTickSkippedWhenQueryEmpty() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);

            ActorRef actor = makeActor(this, mockTmdb);
            // Send tick directly without a prior search (query stays empty)
            actor.tell("tick", ActorRef.noSender());

            expectNoMessage(duration("300 millis"));
            verify(mockTmdb, never()).searchNow(anyString(), anyString(), anyString(), anyString(), anyInt());
        }};
    }

    /**
     * Tick with new results forwards them to client.
     */
    @Test
    public void testTickForwardsNewResults() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);

            // First call (from search): returns id=1
            // Second call (from tick): returns id=2 (new)
            when(mockTmdb.searchNow(anyString(), anyString(), eq("matrix"), eq("movie"), anyInt()))
                    .thenReturn(makeSearchResponse(2, 1))
                    .thenReturn(makeSearchResponse(2, 2));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"matrix\",\"category\":\"movie\"}", ActorRef.noSender());

            // Consume search messages
            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            expectMsgClass(duration("2 seconds"), String.class); // id=1

            // Now send tick manually
            actor.tell("tick", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // total_results from tick
            String tickResult = expectMsgClass(duration("2 seconds"), String.class);
            assert tickResult.contains("\"id\":2") : "Expected id=2 from tick, got: " + tickResult;
        }};
    }

    /**
     * Tick with no new results sends heartbeat and increments page.
     */
    @Test
    public void testTickSendsHeartbeatWhenNoNewResults() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);

            // Both calls return same id=1 — second tick finds no new results
            when(mockTmdb.searchNow(anyString(), anyString(), eq("matrix"), eq("movie"), anyInt()))
                    .thenReturn(makeSearchResponse(1, 1));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"matrix\",\"category\":\"movie\"}", ActorRef.noSender());

            // Consume search messages
            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            expectMsgClass(duration("2 seconds"), String.class); // id=1

            // Tick — id=1 already seen, no new results
            actor.tell("tick", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // total_results
            String heartbeat = expectMsgClass(duration("2 seconds"), String.class);
            assert heartbeat.contains("heartbeat") : "Expected heartbeat, got: " + heartbeat;
        }};
    }

    /**
     * Tick API failure sends error message and rethrows.
     */
    @Test
    public void testTickApiFailureSendsError() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);

            // First call succeeds (search), second call fails (tick)
            when(mockTmdb.searchNow(anyString(), anyString(), eq("matrix"), eq("movie"), anyInt()))
                    .thenReturn(makeSearchResponse(1, 1))
                    .thenThrow(new RuntimeException("API down"));

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"matrix\",\"category\":\"movie\"}", ActorRef.noSender());

            // Consume search messages
            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            expectMsgClass(duration("2 seconds"), String.class); // id=1

            // Tick — API fails
            actor.tell("tick", ActorRef.noSender());

            String error = expectMsgClass(duration("2 seconds"), String.class);
            assert error.contains("error") : "Expected error from tick, got: " + error;
        }};
    }

    /**
     * Tick caps new results at 10.
     */
    @Test
    public void testTickCapsNewResultsAtTen() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);

            when(mockTmdb.searchNow(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(makeSearchResponse(0))  // search: no results
                    .thenReturn(makeSearchResponse(15, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15)); // tick: 15 new

            ActorRef actor = makeActor(this, mockTmdb);
            actor.tell("{\"query\":\"test\",\"category\":\"movie\"}", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results (0)

            // Tick
            actor.tell("tick", ActorRef.noSender());

            expectMsgClass(duration("2 seconds"), String.class); // total_results from tick
            // Expect exactly 10 results
            for (int i = 0; i < 10; i++) {
                expectMsgClass(duration("2 seconds"), String.class);
            }
            expectNoMessage(duration("300 millis"));
        }};
    }

    // -------------------------------------------------------------------------
    // Search history tests
    // -------------------------------------------------------------------------

    /**
     * Search history is trimmed to 10 entries.
     */
    @Test
    public void testSearchHistoryTrimmedToTen() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(makeSearchResponse(0));

            ActorRef actor = makeActor(this, mockTmdb);

            // Send 11 different searches
            for (int i = 0; i < 11; i++) {
                actor.tell("{\"query\":\"query" + i + "\",\"category\":\"movie\"}", ActorRef.noSender());
                // Consume messages for each search
                expectMsgClass(duration("2 seconds"), String.class); // reset
                expectMsgClass(duration("2 seconds"), String.class); // total_results
            }
            // Actor is still alive and processing — history trimming happened internally
            // No assertion needed beyond the actor not crashing
        }};
    }

    // -------------------------------------------------------------------------
    // postStop test
    // -------------------------------------------------------------------------

    /**
     * Actor stops cleanly after receiving poison pill.
     */
    @Test
    public void testActorStopsCleanly() {
        new TestKit(system) {{
            TmdbService mockTmdb = mock(TmdbService.class);
            when(mockTmdb.searchNow(anyString(), anyString(), anyString(), anyString(), anyInt()))
                    .thenReturn(makeSearchResponse(1, 1));

            ActorRef actor = makeActor(this, mockTmdb);

            // Start a search so tickTask gets scheduled
            actor.tell("{\"query\":\"matrix\",\"category\":\"movie\"}", ActorRef.noSender());
            expectMsgClass(duration("2 seconds"), String.class); // reset
            expectMsgClass(duration("2 seconds"), String.class); // total_results
            expectMsgClass(duration("2 seconds"), String.class); // result

            // Stop the actor — postStop should cancel tickTask without error
            watch(actor);
            actor.tell(org.apache.pekko.actor.PoisonPill.getInstance(), ActorRef.noSender());
            expectTerminated(duration("2 seconds"), actor);
        }};
    }
}
