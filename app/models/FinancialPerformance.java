package models;

import java.text.NumberFormat;
import java.util.Locale;

public class FinancialPerformance {

    private long budget;
    private long revenue;

    public FinancialPerformance(long budget, long revenue) {
        this.budget = budget;
        this.revenue = revenue;
    }

    public long getBudget() {
        return budget;
    }

    public long getRevenue() {
        return revenue;
    }

    public long getProfit() {
        return revenue - budget;
    }

    public double getRoiPercent() {
        if (budget == 0) return 0;
        return ((double)(revenue - budget) / budget) * 100;
    }

    public String getFinancialRating() {
        double roi = getRoiPercent();

        if (roi >= 500) return "Blockbuster Success";
        else if (roi >= 200) return "High Return";
        else if (roi >= 0) return "Profitable";
        else return "Financial Loss";
    }

    public String formatMillions(long value) {
        NumberFormat formatter = NumberFormat.getIntegerInstance(Locale.US);
        return formatter.format(value / 1_000_000) + "M";
    }

    public String formatPercent(double value) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format(value);
    }
}