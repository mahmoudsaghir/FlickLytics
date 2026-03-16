package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.GlobalDiversityResult;
import models.Utils;

import java.util.stream.StreamSupport;

/**
 * Service responsible for computing Global Diversity metrics.
 *
 * @author Mahmoud Saghir
 */
public class GlobalDiversityService {

    /**
     * Computes Translation Density and Localization Index.
     *
     * @param category               movie or tv
     * @param detailsRoot            TMDb details response
     * @param translationRoot        TMDb translations response
     * @param targetLanguageConstant normalization constant
     * @return GlobalDiversityResult
     */
    public GlobalDiversityResult compute(String category, JsonNode detailsRoot, JsonNode translationRoot, int targetLanguageConstant) {
        String originalOverview = detailsRoot.path("overview").asText("");

        String mediaName = category.equals("movie")
                ? detailsRoot.path("title").asText("")
                : detailsRoot.path("name").asText("");

        int originalLength = Math.max(originalOverview.length(), 1);

        ArrayNode translationsArray = (ArrayNode) translationRoot.path("translations");

        // Translation Density
        double translationDensity = (double) translationsArray.size() / targetLanguageConstant;

        // Localization Index
        double localizationIndex = StreamSupport.stream(translationsArray.spliterator(), false)
                .map(node -> node.path("data").path("overview").asText(""))
                .filter(overview -> !overview.isEmpty())
                .mapToDouble(overview -> Math.min((double) overview.length() / originalLength, 1.0))
                .average()
                .orElse(0.0);

        return new GlobalDiversityResult(
                Utils.round2(translationDensity),
                Utils.round2(localizationIndex),
                mediaName
        );
    }
}