package models;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit test class for {@link GlobalDiversityResult}.
 *
 * <p>This test verifies that the constructor of {@link GlobalDiversityResult} correctly
 * initializes the fields {@code translationDensity}, {@code localizationIndex}, and {@code mediaName}.</p>
 *
 * <p>Assertions are performed with a tolerance for double values to account for floating-point precision.</p>
 * @author Mahmoud Saghir
 */
public class GlobalDiversityResultTest {

    /**
     * Tests the constructor of {@link GlobalDiversityResult} and ensures that
     * all fields are properly set to the values provided.
     * @author Mahmoud Saghir
     */
    @Test
    public void testConstructorAndFields() {
        double translationDensity = 0.75;
        double localizationIndex = 0.60;
        String mediaName = "Example Movie";

        GlobalDiversityResult result = new GlobalDiversityResult(translationDensity, localizationIndex, mediaName);

        assertEquals(translationDensity, result.translationDensity, 0.0001);
        assertEquals(localizationIndex, result.localizationIndex, 0.0001);
        assertEquals(mediaName, result.mediaName);
    }
}
