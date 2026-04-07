package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import models.Utils;

import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Service responsible for loading and caching genre mappings.
 *
 * @author Mahmoud Saghir
 */
public class GenreService {

    /**
     * Loads movie and TV genres from TMDb API and populates the provided maps.
     *
     * @param apiUrl The TMDb API base URL
     * @param token Bearer token for authorization
     * @param movieGenres Map to populate with movie genres
     * @param tvGenres Map to populate with TV genres
     * @throws java.lang.Exception if the API request fails
     * @author Mahmoud Saghir
     */
    public void loadGenres(String apiUrl,
                           String token,
                           Map<Integer, String> movieGenres,
                           Map<Integer, String> tvGenres) throws Exception {

        loadGenreCategory(apiUrl, token, "movie", movieGenres);
        loadGenreCategory(apiUrl, token, "tv", tvGenres);
    }

    /**
     * Loads genres for a specific category (movie or TV) from TMDb API and populates the provided map.
     *
     * @param apiUrl The TMDb API base URL
     * @param token Bearer token for authorization
     * @param category The category to load genres for ("movie" or "tv")
     * @param genreMap Map to populate with genre mappings
     * @throws java.lang.Exception if the API request fails
     * @author Mahmoud Saghir
     */
    private void loadGenreCategory(String apiUrl,
                                   String token,
                                   String category,
                                   Map<Integer, String> genreMap) throws Exception {

        String genreUrl = apiUrl + "genre/" + category + "/list";

        JsonNode root = Utils.sendGetRequest(genreUrl, token);

        ArrayNode genresArray = (ArrayNode) root.get("genres");

        StreamSupport.stream(genresArray.spliterator(), false)
                .forEach(node -> {
                    int id = node.path("id").asInt();
                    String name = node.path("name").asText();
                    genreMap.put(id, name);
                });
    }
}