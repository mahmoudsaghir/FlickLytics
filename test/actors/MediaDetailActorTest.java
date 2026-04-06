package actors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.libs.Json;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for MediaDetailsActor and MediaDetailsFilter logic.
 *
 * Actor behavior tests verify end-to-end message routing.
 * Filter logic tests are plain Java — fully coverable by JaCoCo.
 *
 * @author Zenghui WU
 */
public class MediaDetailActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("MediaDetailsActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private ObjectNode makeItem(String id, String type, String title) {
        ObjectNode node = Json.newObject();
        node.put("id",      id);
        node.put("type",    type);
        node.put("title",   title);
        node.put("overview", "overview of " + title);
        return node;
    }

    private ObjectNode makeItem(String id, String type, String title, String name, String overview) {
        ObjectNode node = Json.newObject();
        node.put("id",       id);
        node.put("type",     type);
        node.put("title",    title);
        node.put("name",     name);
        node.put("overview", overview);
        return node;
    }

    // =========================================================================
    // Actor behavior tests (end-to-end, existing tests unchanged)
    // =========================================================================

    @Test
    public void testStartSessionWithEmptySeedForwardsNothing() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testStartSessionPushesAllSeedItems() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            List<ObjectNode> seeds = Arrays.asList(
                    makeItem("1", "movie", "Fight Club"),
                    makeItem("2", "movie", "Inception")
            );

            actor.tell(new MediaDetailsActor.StartSession("movie", "", seeds), ActorRef.noSender());

            ObjectNode msg1 = expectMsgClass(duration("1 second"), ObjectNode.class);
            ObjectNode msg2 = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("1", msg1.path("id").asText());
            assertEquals("2", msg2.path("id").asText());
            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testStartSessionDeduplicatesSeedItems() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            ObjectNode item = makeItem("42", "movie", "Duplicate");
            List<ObjectNode> seeds = Arrays.asList(item, item);

            actor.tell(new MediaDetailsActor.StartSession("movie", "", seeds), ActorRef.noSender());

            expectMsgClass(duration("1 second"), ObjectNode.class);
            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testNewItemAfterStartSessionDeduplicatesSeedId() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            ObjectNode seed = makeItem("99", "movie", "Already Seen");
            actor.tell(new MediaDetailsActor.StartSession("movie", "", Arrays.asList(seed)), ActorRef.noSender());
            expectMsgClass(duration("1 second"), ObjectNode.class);

            actor.tell(new MediaDetailsActor.NewItem(seed), ActorRef.noSender());
            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testNewItemMatchingTypeIsForwarded() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            ObjectNode item = makeItem("550", "movie", "Fight Club");
            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());

            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("550", received.path("id").asText());
        }};
    }

    @Test
    public void testNewItemNonMatchingTypeIsFiltered() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            ObjectNode tvItem = makeItem("1396", "tv", "Breaking Bad");
            actor.tell(new MediaDetailsActor.NewItem(tvItem), ActorRef.noSender());

            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testTvActorAcceptsTvRejectsMovie() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "tv"));

            actor.tell(new MediaDetailsActor.StartSession("tv", "", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(makeItem("550",  "movie", "Fight Club")),  ActorRef.noSender());
            actor.tell(new MediaDetailsActor.NewItem(makeItem("1396", "tv",    "Breaking Bad")), ActorRef.noSender());

            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("1396", received.path("id").asText());
            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testAllFilterAcceptsBothTypes() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "all"));

            actor.tell(new MediaDetailsActor.StartSession("all", "", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(makeItem("1", "movie", "Movie A")), ActorRef.noSender());
            actor.tell(new MediaDetailsActor.NewItem(makeItem("2", "tv",    "TV B")),    ActorRef.noSender());

            ObjectNode r1 = expectMsgClass(duration("1 second"), ObjectNode.class);
            ObjectNode r2 = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("1", r1.path("id").asText());
            assertEquals("2", r2.path("id").asText());
        }};
    }

    @Test
    public void testNewItemMatchesViaLinkPrefix() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            ObjectNode item = Json.newObject();
            item.put("id",    "777");
            item.put("link",  "/movie/777");
            item.put("title", "Link Movie");

            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());

            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("777", received.path("id").asText());
        }};
    }

    @Test
    public void testNewItemMatchingQueryTitleIsForwarded() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "fight", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(makeItem("550", "movie", "Fight Club")), ActorRef.noSender());

            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("550", received.path("id").asText());
        }};
    }

    @Test
    public void testNewItemNotMatchingQueryIsFiltered() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "batman", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(makeItem("550", "movie", "Fight Club")), ActorRef.noSender());

            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testQueryFilterMatchesOverviewCaseInsensitive() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "INSOMNIAC", Collections.emptyList()), ActorRef.noSender());

            ObjectNode item = makeItem("550", "movie", "Fight Club", "", "a ticking-time-bomb insomniac");
            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());

            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("550", received.path("id").asText());
        }};
    }

    @Test
    public void testQueryFilterMatchesNameField() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "tv"));

            actor.tell(new MediaDetailsActor.StartSession("tv", "breaking", Collections.emptyList()), ActorRef.noSender());

            ObjectNode item = makeItem("1396", "tv", "", "Breaking Bad", "");
            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());

            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("1396", received.path("id").asText());
        }};
    }

    @Test
    public void testNewItemDeduplicationPreventsDoubleForward() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            ObjectNode item = makeItem("550", "movie", "Fight Club");
            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());
            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());

            expectMsgClass(duration("1 second"), ObjectNode.class);
            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testDifferentItemsAreAllForwarded() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(makeItem("1", "movie", "A")), ActorRef.noSender());
            actor.tell(new MediaDetailsActor.NewItem(makeItem("2", "movie", "B")), ActorRef.noSender());
            actor.tell(new MediaDetailsActor.NewItem(makeItem("3", "movie", "C")), ActorRef.noSender());

            expectMsgClass(duration("1 second"), ObjectNode.class);
            expectMsgClass(duration("1 second"), ObjectNode.class);
            expectMsgClass(duration("1 second"), ObjectNode.class);
            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testNewItemWithEmptyIdIsNeverForwarded() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            ObjectNode item = Json.newObject();
            item.put("id",    "");
            item.put("type",  "movie");
            item.put("title", "No ID");
            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());

            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testChangeSearchClearsSeenIds() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            ObjectNode item = makeItem("550", "movie", "Fight Club");

            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());
            expectMsgClass(duration("1 second"), ObjectNode.class);

            actor.tell(new MediaDetailsActor.ChangeSearch("movie", "", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(item), ActorRef.noSender());
            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("550", received.path("id").asText());
        }};
    }

    @Test
    public void testChangeSearchSwitchesTypeFilter() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(makeItem("1396", "tv", "Breaking Bad")), ActorRef.noSender());
            expectNoMessage(duration("300 millis"));

            actor.tell(new MediaDetailsActor.ChangeSearch("tv", "", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(makeItem("1396", "tv", "Breaking Bad")), ActorRef.noSender());
            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("1396", received.path("id").asText());
        }};
    }

    @Test
    public void testChangeSearchPushesNewSeedBatch() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            List<ObjectNode> newSeeds = Arrays.asList(
                    makeItem("10", "movie", "Seed A"),
                    makeItem("11", "movie", "Seed B")
            );

            actor.tell(new MediaDetailsActor.ChangeSearch("movie", "", newSeeds), ActorRef.noSender());

            ObjectNode s1 = expectMsgClass(duration("1 second"), ObjectNode.class);
            ObjectNode s2 = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("10", s1.path("id").asText());
            assertEquals("11", s2.path("id").asText());
            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testChangeSearchAppliesQueryFilter() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.ChangeSearch("movie", "inception", Collections.emptyList()), ActorRef.noSender());

            actor.tell(new MediaDetailsActor.NewItem(makeItem("550",   "movie", "Fight Club")), ActorRef.noSender());
            actor.tell(new MediaDetailsActor.NewItem(makeItem("27205", "movie", "Inception")),  ActorRef.noSender());

            ObjectNode received = expectMsgClass(duration("1 second"), ObjectNode.class);
            assertEquals("27205", received.path("id").asText());
            expectNoMessage(duration("300 millis"));
        }};
    }

    @Test
    public void testStopMessageTerminatesActor() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            watch(actor);
            actor.tell(MediaDetailsActor.STOP, ActorRef.noSender());
            expectTerminated(duration("2 seconds"), actor);
        }};
    }

    @Test
    public void testNoMessagesAfterStop() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(
                    MediaDetailsActor.props(getRef(), "movie"));

            actor.tell(new MediaDetailsActor.StartSession("movie", "", Collections.emptyList()), ActorRef.noSender());
            actor.tell(MediaDetailsActor.STOP, ActorRef.noSender());

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}

            actor.tell(new MediaDetailsActor.NewItem(makeItem("999", "movie", "After Stop")), ActorRef.noSender());
            expectNoMessage(duration("300 millis"));
        }};
    }



    // -------------------------------------------------------------------------
    // Filter unit tests — every branch hit, JaCoCo sees all of them
    // -------------------------------------------------------------------------

    @Test
    public void testFilterAcceptsMatchingType() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "");
        assertTrue(f.accept(makeItem("1", "movie", "Fight Club")));
    }

    @Test
    public void testFilterRejectsNonMatchingType() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "");
        assertFalse(f.accept(makeItem("1", "tv", "Breaking Bad")));
    }

    @Test
    public void testFilterAllTypeAcceptsBothMovieAndTv() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("all", "");
        assertTrue(f.accept(makeItem("1", "movie", "A")));
        assertTrue(f.accept(makeItem("2", "tv",    "B")));
    }

    @Test
    public void testFilterDeduplicatesSameId() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "");
        assertTrue(f.accept(makeItem("1", "movie", "A")));
        assertFalse(f.accept(makeItem("1", "movie", "A")));
    }

    @Test
    public void testFilterRejectsEmptyId() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "");
        ObjectNode n = Json.newObject();
        n.put("id",   "");
        n.put("type", "movie");
        assertFalse(f.accept(n));
    }

    @Test
    public void testFilterQueryMatchesTitle() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "fight");
        assertTrue(f.accept(makeItem("1", "movie", "Fight Club")));
        assertFalse(f.accept(makeItem("2", "movie", "Inception")));
    }

    @Test
    public void testFilterQueryMatchesOverviewCaseInsensitive() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "INSOMNIAC");
        ObjectNode n = makeItem("1", "movie", "Fight Club");
        n.put("overview", "a ticking-time-bomb insomniac");
        assertTrue(f.accept(n));
    }

    @Test
    public void testFilterQueryMatchesNameField() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("tv", "breaking");
        ObjectNode n = makeItem("1396", "tv", "", "Breaking Bad", "");
        assertTrue(f.accept(n));
    }

    @Test
    public void testFilterLinkPrefixFallbackWhenNoTypeField() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "");
        ObjectNode n = Json.newObject();
        n.put("id",    "777");
        n.put("link",  "/movie/777");
        n.put("title", "Link Movie");
        assertTrue(f.accept(n));
    }

    @Test
    public void testFilterLinkPrefixRejectedWhenWrongType() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "");
        ObjectNode n = Json.newObject();
        n.put("id",   "888");
        n.put("link", "/tv/888");
        assertFalse(f.accept(n));
    }

    @Test
    public void testFilterResetClearsSeenIds() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "");
        f.accept(makeItem("1", "movie", "A"));
        f.reset("movie", "");
        assertTrue(f.accept(makeItem("1", "movie", "A")));
    }

    @Test
    public void testFilterResetSwitchesTypeFilter() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "");
        assertFalse(f.accept(makeItem("1", "tv", "B")));
        f.reset("tv", "");
        assertTrue(f.accept(makeItem("2", "tv", "B")));
    }

    @Test
    public void testFilterResetSwitchesQueryFilter() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "batman");
        assertFalse(f.accept(makeItem("1", "movie", "Fight Club")));
        f.reset("movie", "fight");
        assertTrue(f.accept(makeItem("2", "movie", "Fight Club")));
    }

    @Test
    public void testFilterResetWithNullQueryDefaultsToEmpty() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", null);
        assertTrue(f.accept(makeItem("1", "movie", "Anything")));
        f.reset("movie", null);
        assertTrue(f.accept(makeItem("2", "movie", "Anything Else")));
    }

    @Test
    public void testFilterQueryNoMatchOnAnyField() {
        MediaDetailsActor.MediaDetailsFilter f = new MediaDetailsActor.MediaDetailsFilter("movie", "xyz");
        ObjectNode n = makeItem("1", "movie", "", "", "no match here");
        assertFalse(f.accept(n));
    }
}