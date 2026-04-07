package actors;

import models.FinancialPerformance;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for FinancialPerformanceActor
 * Verifies that the actor correctly handles messages
 * and responds with object containing the expected budget and revenue.
 * Uses Pekko TestKit for isolated actor testing.
 *
 * @author Charles Wang
 */

public class FinancialPerformanceActorTest {

    /** Shared ActorSystem for all tests.*/
    private static ActorSystem system;

    /**
     * Initializes the ActorSystem before all tests.
     */
    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("TestSystem");
    }

    /**
     * Shuts down the ActorSystem after all tests.
     */
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    /**
     * Checks that FinancialPerformanceActor returns a FinancialPerformance
     * object with the same budget and revenue values when sent a
     * Calculate message with positive amounts.
     *
     * @author Charles Wang
     */

    @Test
    public void testCalculateMessageReturnsFinancialPerformance() {
        new TestKit(system) {{
            // Create the actor
            final ActorRef actor = system.actorOf(FinancialPerformanceActor.props());

            // Send a Calculate message
            long budget = 100_000_000;
            long revenue = 300_000_000;
            actor.tell(new FinancialPerformanceActor.Calculate(budget, revenue), getRef());

            // Expect a FinancialPerformance reply
            FinancialPerformance fp = expectMsgClass(FinancialPerformance.class);

            // Verify that the values match
            assertEquals(budget, fp.getBudget());
            assertEquals(revenue, fp.getRevenue());
        }};
    }

    /**
     * Checks that FinancialPerformanceActor correctly handles a Calculate
     * message with zero values.
     * Ensures the actor can handle edge cases and responds with zero budget and revenue.
     *
     * @author Charles Wang
     */

    @Test
    public void testCalculateWithZeroValues() {
        new TestKit(system) {{
            final ActorRef actor = system.actorOf(FinancialPerformanceActor.props());

            actor.tell(new FinancialPerformanceActor.Calculate(0, 0), getRef());

            FinancialPerformance fp = expectMsgClass(FinancialPerformance.class);

            assertEquals(0, fp.getBudget());
            assertEquals(0, fp.getRevenue());
        }};
    }
}