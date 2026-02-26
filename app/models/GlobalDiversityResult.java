package models;

/**
 * Data holder for Global Diversity metrics.
 *
 * @author Mahmoud Saghir
 */
public class GlobalDiversityResult {

    public final double translationDensity;
    public final double localizationIndex;
    public final String mediaName;

    public GlobalDiversityResult(double translationDensity, double localizationIndex, String mediaName) {
        this.translationDensity = translationDensity;
        this.localizationIndex = localizationIndex;
        this.mediaName = mediaName;
    }
}