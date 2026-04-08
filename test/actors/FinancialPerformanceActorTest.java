package actors;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for FinancialPerformanceActor
 * Verifies that the actor performs financial calculations correctly
 * and returns a Result object with computed values.
 *
 * @author Charles Wang
 */

public class FinancialPerformanceActorTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("TestSystem");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Test normal calculation (positive values)
     *
     * @author Charles Wang
     */

    @Test
    public void testCalculateMessageReturnsComputedResult() {
        new TestKit(system) {{
            final ActorRef actor = system.actorOf(FinancialPerformanceActor.props());

            long budget = 100_000_000;
            long revenue = 300_000_000;

            actor.tell(new FinancialPerformanceActor.Calculate(budget, revenue), getRef());

            FinancialPerformanceActor.Result result =
                    expectMsgClass(FinancialPerformanceActor.Result.class);

            assertEquals(budget, result.budget);
            assertEquals(revenue, result.revenue);
            assertEquals(200_000_000, result.profit);
            assertEquals(200.0, result.roi, 0.01);
            assertEquals("High Return", result.rating);
        }};
    }

    /**
     * Test edge case: zero values
     *
     * @author Charles Wang
     */

    @Test
    public void testCalculateWithZeroValues() {
        new TestKit(system) {{
            final ActorRef actor = system.actorOf(FinancialPerformanceActor.props());

            actor.tell(new FinancialPerformanceActor.Calculate(0, 0), getRef());

            FinancialPerformanceActor.Result result =
                    expectMsgClass(FinancialPerformanceActor.Result.class);

            assertEquals(0, result.budget);
            assertEquals(0, result.revenue);
            assertEquals(0, result.profit);
            assertEquals(0.0, result.roi, 0.01);
            assertEquals("Profitable", result.rating); // ROI = 0 → falls in >= 0 case
        }};
    }
}