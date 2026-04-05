package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Readability;

/**
 * Service responsible for fetching media details from the TMDb API
 * and computing readability scores for the overview text.
 *
 * @author Zenghui WU
 * @author Tasmia Naomi
 */
public class MediaDetailsService {
    /*this class is the service layer between:
    * 1. the controller
    * 2. the TMDb API service
    * 3. the readability model
    *
    * 1. fetch the media details from TMDb
    * 2. extract the overview text
    * 3. compute the readbaility scores
    * 4. package everything into one result object
    *  */
    private final TmdbService tmdbService;

    /**
     * Constructs the MediaDetailsService with a TmdbService dependency.
     *
     * @param tmdbService The service for TMDb API communications
     * @author Zenghui WU
     */
    @Inject
    public MediaDetailsService(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    //create the service by


    /**
     * Holds the result of a details fetch with readability analysis.
     * Contains the raw JSON details, the overview text, and computed readability scores.
     *
     * @author Zenghui WU
     */
    public static class DetailsResult {
        /** The raw JSON details from the TMDb API. */
        public final JsonNode details;
        /** The overview text extracted from the details. */
        public final String overview;
        /** The computed Flesch readability scores for the overview. */
        public final Readability.ReadabilityScores scores;

        /**
         * Constructs a DetailsResult with the given details, overview, and scores.
         *
         * @param details The raw JSON details from the TMDb API
         * @param overview The overview text
         * @param scores The computed readability scores
         * @author Zenghui WU
         */
        public DetailsResult(JsonNode details, String overview, Readability.ReadabilityScores scores) {
            this.details = details;
            this.overview = overview;
            this.scores = scores;
        }
    }

    /**
     * Fetches details for a movie or TV show and computes readability scores
     * for the overview text using the Flesch-Kincaid formulas.
     *
     * @param apiUrl The TMDb API base URL
     * @param token Bearer token for authorization
     * @param type The media type ("movie" or "tv")
     * @param id The TMDb media ID
     * @return A DetailsResult containing the details, overview, and readability scores
     * @throws java.lang.Exception if the API request fails
     * @author Zenghui WU
     */
    public DetailsResult getDetailsWithReadability(String apiUrl, String token, String type, Long id) throws Exception {
        JsonNode details = tmdbService.getDetails(apiUrl, token, type, id);
        String overview = details.path("overview").asText("");
        Readability.ReadabilityScores scores = Readability.compute(overview);
        return new DetailsResult(details, overview, scores);
    }
}