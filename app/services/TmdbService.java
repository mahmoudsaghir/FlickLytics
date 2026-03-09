package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import models.Utils;

/**
 * Service responsible for all TMDb API communications.
 *
 * This service centralizes external HTTP calls and prevents business logic
 * inside controllers.
 *
 * @author Mahmoud Saghir
 */
public class TmdbService {
    /**
     * Performs search request.
     */
    public JsonNode search(String apiUrl, String token, String query, String category) throws Exception {

        String encodedQuery = query.replace(" ", "%20");

        String searchUrl = apiUrl + "search/";

        switch (category) {
            case "movie" -> searchUrl += "movie?query=" + encodedQuery;
            case "tv" -> searchUrl += "tv?query=" + encodedQuery;
            case "person" -> searchUrl += "person?query=" + encodedQuery;
        }

        return Utils.sendGetRequest(searchUrl, token);
    }

    /**
     * Fetches details for movie or tv.
     */
    public JsonNode getDetails(String apiUrl, String token, String type, Long id) throws Exception {
        String url = apiUrl + type + "/" + id + "?language=en-US";
        return Utils.sendGetRequest(url, token);
    }

    /**
     * Fetches translations list.
     */
    public JsonNode getTranslations(String apiUrl, String token, String type, Long id) throws Exception {
        String url = apiUrl + type + "/" + id + "/translations";
        return Utils.sendGetRequest(url, token);
    }

    /**
     * Fetches combined credits (cast + crew) for a person by their TMDb ID.
     * Used to retrieve the "known for" items for person statistics.
     *
     * @param apiUrl The TMDb API base URL
     * @param token Bearer token for authorization
     * @param personId The TMDb person ID
     * @return JsonNode containing cast and crew arrays
     * @throws Exception if the API request fails
     * @author Syed Shahab Shah
     */
    public JsonNode getPersonCredits(String apiUrl, String token, String personId) throws Exception {
        String url = apiUrl + "person/" + personId + "/combined_credits";
        return Utils.sendGetRequest(url, token);
    }

    /**
     * Loads collection translation count constant.
     */
    public int loadTargetLanguageConstant(String apiUrl, String token) {
        try {
            JsonNode root = Utils.sendGetRequest(apiUrl + "collection/10/translations", token);

            JsonNode translations = root.path("translations");

            if (translations.isArray()) {
                return translations.size();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }

    /**
     * Fetches reviews for a movie or TV show, paginating up to 3 pages
     * to collect up to 60 raw results (the service layer will limit to 50).
     * TMDb returns 20 reviews per page, so 3 pages are needed for up to 50 reviews.
     *
     * @param apiUrl The TMDb API base URL
     * @param token Bearer token for authorization
     * @param type The media type ("movie" or "tv")
     * @param id The TMDb media ID
     * @return JsonNode containing a merged "results" array from up to 3 pages
     * @throws Exception if the API request fails
     * @author Tasmia Naomi
     */
    public JsonNode getReviews(String apiUrl, String token, String type, Long id) throws Exception {
        String baseUrl = apiUrl + type + "/" + id + "/reviews";

        // Fetch the first page
        JsonNode firstPage = Utils.sendGetRequest(baseUrl + "?page=1", token);
        int totalPages = firstPage.path("total_pages").asInt(1);

        // If only 1 page, return as-is
        if (totalPages <= 1) {
            return firstPage;
        }

        // Merge results from up to 3 pages
        com.fasterxml.jackson.databind.node.ArrayNode mergedResults = play.libs.Json.newArray();
        for (com.fasterxml.jackson.databind.JsonNode item : firstPage.path("results")) {
            mergedResults.add(item);
        }

        int pagesToFetch = Math.min(totalPages, 3);
        for (int page = 2; page <= pagesToFetch; page++) {
            JsonNode nextPage = Utils.sendGetRequest(baseUrl + "?page=" + page, token);
            for (com.fasterxml.jackson.databind.JsonNode item : nextPage.path("results")) {
                mergedResults.add(item);
            }
        }

        // Build a merged response node
        com.fasterxml.jackson.databind.node.ObjectNode merged = play.libs.Json.newObject();
        merged.set("results", mergedResults);
        merged.put("total_results", firstPage.path("total_results").asInt(0));
        merged.put("total_pages", totalPages);
        return merged;
    }
}