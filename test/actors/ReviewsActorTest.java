package actors;

import models.Review;
import models.ReviewsSummary;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import services.ReviewsService;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReviewsActor.
 *
 * @author Tasmia Naomi
 */
public class ReviewsActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("ReviewsActorTestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testComputeReviewsReturnsSummary() throws Exception {
        new TestKit(system) {{
            ReviewsService reviewsService = mock(ReviewsService.class);
            ReviewsSummary expected = new ReviewsSummary(List.of(
                    new Review("User", "Great movie", ":-)", 100.0, 0.0)
            ));
            when(reviewsService.getReviewsWithSentiment("api", "token", "movie", 550L)).thenReturn(expected);

            ActorRef actor = system.actorOf(ReviewsActor.props(reviewsService, "api", "token"));
            actor.tell(new ReviewsActor.ComputeReviews("movie", 550L), getRef());

            ReviewsSummary actual = expectMsgClass(ReviewsSummary.class);
            assertEquals(":-)", actual.getGlobalSentiment());
            assertEquals(1, actual.getTotalReviews());
        }};
    }

    @Test
    public void testComputeReviewsFailureRestartsActor() throws Exception {
        new TestKit(system) {{
            ReviewsService reviewsService = mock(ReviewsService.class);
            when(reviewsService.getReviewsWithSentiment("api", "token", "tv", 1L))
                    .thenThrow(new RuntimeException("boom"));

            ActorRef actor = system.actorOf(ReviewsActor.props(reviewsService, "api", "token"));
            actor.tell(new ReviewsActor.ComputeReviews("tv", 1L), getRef());

            expectNoMessage();
        }};
    }
}


