package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Readability;

public class MediaDetailsService {

    private final TmdbService tmdbService;

    @Inject
    public MediaDetailsService(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    public static class DetailsResult {
        public final JsonNode details;
        public final String overview;
        public final Readability.ReadabilityScores scores;

        public DetailsResult(JsonNode details, String overview, Readability.ReadabilityScores scores) {
            this.details = details;
            this.overview = overview;
            this.scores = scores;
        }
    }

    public DetailsResult getDetailsWithReadability(String apiUrl, String token, String type, Long id) throws Exception {
        JsonNode details = tmdbService.getDetails(apiUrl, token, type, id);
        String overview = details.path("overview").asText("");
        Readability.ReadabilityScores scores = Readability.compute(overview);
        return new DetailsResult(details, overview, scores);
    }
}