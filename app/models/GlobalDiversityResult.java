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

    /**
     * Constructor for GlobalDiversityResult.
     *
     * @param translationDensity The computed Translation Density metric
     * @param localizationIndex The computed Localization Index metric
     * @param mediaName The name of the media item
     * @author Mahmoud Saghir
     */
    public GlobalDiversityResult(double translationDensity, double localizationIndex, String mediaName) {
        this.translationDensity = translationDensity;
        this.localizationIndex = localizationIndex;
        this.mediaName = mediaName;
    }
}