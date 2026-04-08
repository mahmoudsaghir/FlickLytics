package actors;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Pekko Actor (Classic) handling financial performance calculations.
 * Actor performing the business logic.
 *
 * @author Charles Wang
 */

public class FinancialPerformanceActor extends AbstractActor {

    /**
     * Message class for calculation requests.
     */

    public static class Calculate {
        public final long budget;
        public final long revenue;

        public Calculate(long budget, long revenue) {
            this.budget = budget;
            this.revenue = revenue;
        }
    }

    /**
     * Object containing budget and revenue results to send back
     */

    public static class Result {
        public final long budget;
        public final long revenue;
        public final long profit;
        public final double roi;
        public final String rating;
        public final String formattedBudget;
        public final String formattedRevenue;
        public final String formattedProfit;
        public final String formattedRoi;

        public Result(long budget, long revenue, long profit, double roi,
                      String rating, String formattedBudget,
                      String formattedRevenue, String formattedProfit,
                      String formattedRoi) {
            this.budget = budget;
            this.revenue = revenue;
            this.profit = profit;
            this.roi = roi;
            this.rating = rating;
            this.formattedBudget = formattedBudget;
            this.formattedRevenue = formattedRevenue;
            this.formattedProfit = formattedProfit;
            this.formattedRoi = formattedRoi;
        }
    }

    /**
     * Used to create a new FinancialPerformanceActor
     */

    public static Props props() {
        return Props.create(FinancialPerformanceActor.class);
    }

    /**
     * Tells the actor what to do when it receives different types of messages
     */

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Calculate.class, msg -> {
                    try {

                        long profit = msg.revenue - msg.budget;

                        double roi = (msg.budget == 0)
                                ? 0
                                : ((double) profit / msg.budget) * 100;

                        String rating;
                        if (roi >= 500) rating = "Blockbuster Success";
                        else if (roi >= 200) rating = "High Return";
                        else if (roi >= 0) rating = "Profitable";
                        else rating = "Financial Loss";

                        NumberFormat numberFormatter = NumberFormat.getIntegerInstance(Locale.US);
                        NumberFormat percentFormatter = NumberFormat.getNumberInstance(Locale.US);
                        percentFormatter.setMinimumFractionDigits(2);
                        percentFormatter.setMaximumFractionDigits(2);

                        String formattedBudget = numberFormatter.format(msg.budget / 1_000_000) + "M";
                        String formattedRevenue = numberFormatter.format(msg.revenue / 1_000_000) + "M";
                        String formattedProfit = numberFormatter.format(profit / 1_000_000) + "M";
                        String formattedRoi = percentFormatter.format(roi);

                        // Send computed result
                        sender().tell(new Result(
                                msg.budget,
                                msg.revenue,
                                profit,
                                roi,
                                rating,
                                formattedBudget,
                                formattedRevenue,
                                formattedProfit,
                                formattedRoi
                        ), self());

                    } catch (Exception e) {
                        sender().tell(
                                new Result(0, 0, 0, 0, "Error", "0M", "0M", "0M", "0.00"),
                                self()
                        );
                        System.err.println("Actor calculation failed: " + e);
                    }
                })
                .build();
    }
}