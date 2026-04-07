package actors;

import models.MovieOrTVShow;
import models.PersonStats;
import models.Review;
import models.ReviewsSummary;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.Props;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.GlobalDiversityService;
import services.ReviewsService;
import services.TmdbService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SupervisorActor routing.
 *
 * @author Syed Shahab Shah
 * @author Tasmia Naomi
 */
public class SupervisorActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("SupervisorActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testComputePersonStatsRoutedToPersonStatsActor() {
        new TestKit(system) {{
            GlobalDiversityService globalService = mock(GlobalDiversityService.class);
            ReviewsService reviewsService = mock(ReviewsService.class);
            TmdbService tmdbService = mock(TmdbService.class);

            ActorRef supervisor = system.actorOf(
                    Props.create(SupervisorActor.class, () -> new SupervisorActor(globalService, reviewsService, tmdbService, "api", "token"))
            );

            List<MovieOrTVShow> items = new ArrayList<>();
            items.add(new MovieOrTVShow("1", "Movie A", 10.0, 8.0, 100, "2010"));
            items.add(new MovieOrTVShow("2", "Movie B", 20.0, 6.0, 200, "2012"));

            supervisor.tell(
                    new SupervisorActor.ComputePersonStats(
                            items,
                            "Test Person",
                            "/test.jpg",
                            "Acting",
                            2,
                            "",
                            "Los Angeles"
                    ),
                    getRef()
            );

            PersonStats stats = expectMsgClass(PersonStats.class);
            assertEquals("Test Person", stats.getPersonName());
            assertEquals(2, stats.getLatestItems().size());
            assertEquals("Movie B", stats.getLatestItems().get(0).getTitle());
            assertEquals("Movie A", stats.getLatestItems().get(1).getTitle());
        }};
    }

    @Test
    public void testComputeReviewsRoutedToReviewsActor() throws Exception {
        new TestKit(system) {{
            GlobalDiversityService globalService = mock(GlobalDiversityService.class);
            ReviewsService reviewsService = mock(ReviewsService.class);
            TmdbService tmdbService = mock(TmdbService.class);

            ReviewsSummary expected = new ReviewsSummary(List.of(
                    new Review("A", "Great movie", ":-)", 100.0, 0.0)
            ));
            when(reviewsService.getReviewsWithSentiment("api", "token", "movie", 10L)).thenReturn(expected);

            ActorRef supervisor = system.actorOf(
                    Props.create(SupervisorActor.class, () -> new SupervisorActor(globalService, reviewsService, tmdbService, "api", "token"))
            );

            supervisor.tell(new SupervisorActor.ComputeReviews("movie", 10L), getRef());
            ReviewsSummary actual = expectMsgClass(ReviewsSummary.class);
            assertEquals(1, actual.getTotalReviews());
            assertEquals(":-)", actual.getGlobalSentiment());
        }};
    }
    @Test
    public void testCreateSearchActorConstructorSetsFields() {
        new TestKit(system) {{
            ActorRef out = getRef();
            Props props = Props.empty();

            SupervisorActor.CreateSearchActor message =
                    new SupervisorActor.CreateSearchActor(out, props);

            assertEquals(out, message.out);
            assertEquals(props, message.props);
        }};
    }
}

