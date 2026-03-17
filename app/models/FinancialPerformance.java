package models;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Represents the financial performance of a movie.
 * It stores the budget and revenue and provides methods to
 * calculate profit, return on investment (ROI), financial rating,
 * and formatted financial values.
 *
 * @author Charles Wang
 */
public class FinancialPerformance {

    private long budget;
    private long revenue;

    /**
     * Creates a FinancialPerformance object with the specified budget and revenue.
     *
     * @param budget the total production budget
     * @param revenue the total revenue generated
     */
    public FinancialPerformance(long budget, long revenue) {
        this.budget = budget;
        this.revenue = revenue;
    }

    /**
     * Returns the budget amount.
     *
     * @return the budget
     */
    public long getBudget() {
        return budget;
    }

    /**
     * Returns the revenue amount.
     *
     * @return the revenue
     */
    public long getRevenue() {
        return revenue;
    }

    /**
     * Calculates the profit by subtracting the budget from the revenue.
     *
     * @return the profit amount
     */
    public long getProfit() {
        return revenue - budget;
    }

    /**
     * Calculates the Return on Investment (ROI) percentage.
     *
     * @return ROI percentage, or 0 if the budget is 0
     */
    public double getRoiPercent() {
        if (budget == 0) return 0;
        return ((double)(revenue - budget) / budget) * 100;
    }

    /**
     * Determines the financial rating based on ROI percentage.
     *
     * @return a string describing the financial performance category
     */
    public String getFinancialRating() {
        double roi = getRoiPercent();

        if (roi >= 500) return "Blockbuster Success";
        else if (roi >= 200) return "High Return";
        else if (roi >= 0) return "Profitable";
        else return "Financial Loss";
    }

    /**
     * Formats a large number into millions (e.g., 150000000 -> 150M).
     *
     * @param value the number to format
     * @return formatted value in millions
     */
    public String formatMillions(long value) {
        NumberFormat formatter = NumberFormat.getIntegerInstance(Locale.US);
        return formatter.format(value / 1_000_000) + "M";
    }

    /**
     * Formats a percentage value with two decimal places.
     *
     * @param value the percentage value
     * @return formatted percentage string
     */
    public String formatPercent(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value);
    }
}