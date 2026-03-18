package models;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the FinancialPerformance model class.
 * Tests financial calculations such as profit, ROI percentage,
 * and that financial ratings/formatting methods are working.
 *
 * @author Charles Wang
 *
 */

public class FinancialPerformanceTest {
    /**
     * Tests the constructor and that the getters return them.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testConstructorAndGetters() {
        FinancialPerformance fp = new FinancialPerformance(100000000, 300000000);

        assertEquals(100000000, fp.getBudget());
        assertEquals(300000000, fp.getRevenue());
    }

    /**
     * Tests profit calculation.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testProfitCalculation() {
        FinancialPerformance fp = new FinancialPerformance(100000000, 300000000);

        assertEquals(200000000, fp.getProfit());
    }

    /**
     * Tests ROI percentage calculation.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testRoiPercent() {
        FinancialPerformance fp = new FinancialPerformance(100000000, 300000000);

        assertEquals(200.0, fp.getRoiPercent(), 0.01);
    }

    /**
     * Tests when ROI has a budget of zero.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testRoiPercentBudgetZero() {
        FinancialPerformance fp = new FinancialPerformance(0, 100000000);

        assertEquals(0.0, fp.getRoiPercent(), 0.01);
    }

    /**
     * Tests the financial rating when ROI is equal or above 500%.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testFinancialRatingBlockbuster() {
        FinancialPerformance fp = new FinancialPerformance(100000000, 700000000);

        assertEquals("Blockbuster Success", fp.getFinancialRating());
    }

    /**
     * Tests the financial rating when ROI is equal or above 200%.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testFinancialRatingHighReturn() {
        FinancialPerformance fp = new FinancialPerformance(100000000, 300000000);

        assertEquals("High Return", fp.getFinancialRating());
    }

    /**
     * Tests the financial rating when ROI is positive but below 200%.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testFinancialRatingProfitable() {
        FinancialPerformance fp = new FinancialPerformance(100000000, 150000000);

        assertEquals("Profitable", fp.getFinancialRating());
    }

    /**
     * Tests the financial rating when ROI is negative.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testFinancialRatingLoss() {
        FinancialPerformance fp = new FinancialPerformance(100000000, 50000000);

        assertEquals("Financial Loss", fp.getFinancialRating());
    }

    /**
     * Tests formatting of large numbers into millions.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testFormatMillions() {
        FinancialPerformance fp = new FinancialPerformance(0,0);

        assertEquals("150M", fp.formatMillions(150000000));
    }

    /**
     * Tests percentage formatting to two decimal places.
     *
     * @author Charles Wang
     *
     */

    @Test
    public void testFormatPercent() {
        FinancialPerformance fp = new FinancialPerformance(0,0);

        assertEquals("123.46", fp.formatPercent(123.456));
    }

}