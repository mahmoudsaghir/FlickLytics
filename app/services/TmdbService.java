package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Utils;
import play.libs.Json;

/**
 * Service responsible for all TMDb API communications.
 * This service centralizes external HTTP calls and prevents business logic
 * inside controllers.
 *
 * @author Mahmoud Saghir
 */
public class TmdbService {
    /**
     * Performs search request.
     *
     * @param apiUrl   The TMDb API base URL
     * @param token    Bearer token for authorization
     * @param query    The search query string
     * @param category The category to search in ("movie", "tv", or "person")
     * @param currentPage The page number for pagination
     * @return JsonNode containing search results
     * @throws java.lang.Exception if the API request fails
     * @author Mahmoud Saghir
     */
    public JsonNode search(String apiUrl, String token, String query, String category, int currentPage) throws Exception {

        String encodedQuery = query.replace(" ", "%20");

        String searchUrl = apiUrl + "search/";

        switch (category) {
            case "movie" -> { return Utils.sendGetRequest(searchUrl + "movie?query=" + encodedQuery + "&page=" + currentPage, token); }
            case "tv"    -> { return Utils.sendGetRequest(searchUrl + "tv?query=" + encodedQuery + "&page=" + currentPage, token); }
            case "person"-> { return Utils.sendGetRequest(searchUrl + "person?query=" + encodedQuery + "&page=" + currentPage, token); }
            default      -> { return Utils.sendGetRequest(searchUrl, token); }
        }

       // return Utils.sendGetRequest(searchUrl + "&page=" + currentPage, token);
    }

    /**
     * Fetches details for movie or tv.
     *
     * @param apiUrl The TMDb API base URL
     * @param token  Bearer token for authorization
     * @param type   movie or tv
     * @param id     TMDb media ID
     * @return JsonNode containing media details
     * @throws java.lang.Exception if the API request fails
     * @author Mahmoud Saghir
     */
    public JsonNode getDetails(String apiUrl, String token, String type, Long id) throws Exception {
        String url = apiUrl + type + "/" + id + "?language=en-US";
        return Utils.sendGetRequest(url, token);
    }

    /**
     * Fetches details with appended translations (using TMDb's append_to_response feature).
     *
     * @param apiUrl The TMDb API base URL
     * @param token  Bearer token for authorization
     * @param type   movie or tv
     * @param id     TMDb media ID
     * @return JsonNode containing details and translations in a single response
     * @throws java.lang.Exception if the API request fails
     * @author Mahmoud Saghir
     */
    public JsonNode getDetailsAndTranslations(String apiUrl, String token, String type, Long id) throws Exception {
        String url = apiUrl + type + "/" + id + "?append_to_response=translations&language=en-US";
        return Utils.sendGetRequest(url, token);
    }

    /**
     * Fetches combined credits (cast + crew) for a person by their TMDb ID.
     * Used to retrieve the "known for" items for person statistics.
     *
     * @param apiUrl   The TMDb API base URL
     * @param token    Bearer token for authorization
     * @param personId The TMDb person ID
     * @return JsonNode containing cast and crew arrays
     * @throws java.lang.Exception if the API request fails
     * @author Syed Shahab Shah
     */
    public JsonNode getPersonCredits(String apiUrl, String token, String personId) throws Exception {
        String url = apiUrl + "person/" + personId + "/combined_credits";
        return Utils.sendGetRequest(url, token);
    }

    /**
     * Fetches personal details for a person by their TMDb ID.
     * Returns name, photo, gender, birthday, place of birth, known_for_department, etc.
     *
     * @param apiUrl   The TMDb API base URL
     * @param token    Bearer token for authorization
     * @param personId The TMDb person ID
     * @return JsonNode containing person details
     * @throws java.lang.Exception if the API request fails
     * @author Syed Shahab Shah
     */
    public JsonNode getPersonDetails(String apiUrl, String token, String personId) throws Exception {
        String url = apiUrl + "person/" + personId;
        return Utils.sendGetRequest(url, token);
    }

    /**
     * Loads collection translation count constant.
     *
     * @param apiUrl The TMDb API base URL
     * @param token  Bearer token for authorization
     * @return The number of translations for the collection with ID 10, or 1 if an error occurs
     * @author Mahmoud Saghir
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
     * @param token  Bearer token for authorization
     * @param type   The media type ("movie" or "tv")
     * @param id     The TMDb media ID
     * @return JsonNode containing a merged "results" array from up to 3 pages
     * @throws java.lang.Exception if the API request fails
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

    /**
     * Fetches search results immediately (used by WebSocket reactive updates).
     * Returns the list of results array from TMDb response.
     *
     * @author Mahmoud Saghir
     */
    public ObjectNode searchNow(String apiUrl, String token, String query, String category, int currentPage) {
        ObjectNode resultNode = Json.newObject();

        try {
            JsonNode response = search(apiUrl, token, query, category, currentPage);

            if (response.has("results") && response.get("results").isArray()) {
                resultNode.set("results", response.get("results"));
            } else {
                resultNode.set("results", Json.newArray());
            }

            resultNode.put("total_results", response.path("total_results").asInt(0));

        } catch (Exception e) {
            e.printStackTrace();
            resultNode.set("results", Json.newArray());
            resultNode.put("total_results", 0);
        }

        return resultNode;
    }
}