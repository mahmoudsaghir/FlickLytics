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
}