package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import models.FinancialPerformance;

/**
 * Classic Pekko Actor for handling financial performance calculations.
 * Delegates all business logic to the FinancialPerformance model.
 *
 * @author Charles Wang
 */

public class FinancialPerformanceActor extends AbstractActor {

    /**
     * Message class for calculation requests.
     * Immutable data container for budget and revenue.
     *
     * @author Charles Wang
     */

    public static class Calculate {
        public final long budget;
        public final long revenue;

        /**
         * Constructor for Calculate message
         * @param budget Movie budget
         * @param revenue Movie revenue
         */

        public Calculate(long budget, long revenue) {
            this.budget = budget;
            this.revenue = revenue;
        }
    }

    /**
     * Factory method to create Props (configuration object for creating an actor) for this actor.
     */
    public static Props props() {
        return Props.create(FinancialPerformanceActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Calculate.class, msg -> {
                    try {
                        // Delegate to model (GOOD PRACTICE)
                        FinancialPerformance fp = new FinancialPerformance(msg.budget, msg.revenue);

                        // Send result back to sender (controller)
                        sender().tell(fp, self());
                    } catch (Exception e) {
                        // Error handling: send a fallback object
                        FinancialPerformance fallback = new FinancialPerformance(0, 0);
                        sender().tell(fallback, self());

                        // Optional: log the error
                        getContext().getSystem().log().error("Failed to calculate FinancialPerformance", e);
                    }
                })
                .build();
    }
}