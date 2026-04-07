package actors;

import models.MovieOrTVShow;
import models.PersonStats;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for PersonStatsActor reactive behavior.
 *
 * @author Syed Shahab Shah
 */
public class PersonStatsActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("PersonStatsActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testSetItemsAndGetSnapshot() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(PersonStatsActor.props(null));

            List<MovieOrTVShow> items = new ArrayList<>();
            items.add(new MovieOrTVShow("1", "A", 10.0, 7.0, 100));
            items.add(new MovieOrTVShow("2", "B", 30.0, 9.0, 300));

            actor.tell(new PersonStatsActor.SetItems(items), getRef());
            actor.tell(new PersonStatsActor.GetSnapshot(), getRef());

            PersonStatsActor.PersonStatsSnapshot snapshot = expectMsgClass(PersonStatsActor.PersonStatsSnapshot.class);
            assertEquals(2, snapshot.latestItems.size());
            assertEquals(20.0, snapshot.popAvg, 0.01);
            assertEquals(10.0, snapshot.popMin, 0.01);
            assertEquals(30.0, snapshot.popMax, 0.01);
            assertEquals(8.0, snapshot.voteAvg, 0.01);
            assertEquals(100, snapshot.countMin);
            assertEquals(300, snapshot.countMax);
        }};
    }

    @Test
    public void testSetPersonDetailsAndGetSnapshot() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(PersonStatsActor.props(new ArrayList<>()));

            actor.tell(new PersonStatsActor.SetPersonDetails("John Doe", "/john.jpg", "Acting", 2, "", "Montreal"), getRef());
            actor.tell(new PersonStatsActor.GetSnapshot(), getRef());

            PersonStatsActor.PersonStatsSnapshot snapshot = expectMsgClass(PersonStatsActor.PersonStatsSnapshot.class);
            assertEquals("John Doe", snapshot.personName);
            assertEquals("https://image.tmdb.org/t/p/w300/john.jpg", snapshot.profilePhotoUrl);
            assertEquals("Acting", snapshot.knownFor);
            assertEquals("Male", snapshot.gender);
            assertEquals("N/A", snapshot.birthday);
            assertEquals(-1, snapshot.age);
            assertEquals("Montreal", snapshot.placeOfBirth);
        }};
    }

    @Test
    public void testComputePersonStatsReturnsModel() {
        new TestKit(system) {{
            ActorRef actor = system.actorOf(PersonStatsActor.props(new ArrayList<>()));

            List<MovieOrTVShow> items = new ArrayList<>();
            items.add(new MovieOrTVShow("1", "A", 10.0, 7.0, 100, "2012"));

            actor.tell(new PersonStatsActor.ComputePersonStats(
                    items,
                    "John Doe",
                    "/john.jpg",
                    "Acting",
                    2,
                    "",
                    "Montreal"
            ), getRef());

            PersonStats stats = expectMsgClass(PersonStats.class);
            assertEquals("John Doe", stats.getPersonName());
            assertEquals(1, stats.getLatestItems().size());
            assertEquals("A", stats.getLatestItems().get(0).getTitle());
        }};
    }
}


