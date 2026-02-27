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

    public void loadGenres(String apiUrl,
                           String token,
                           Map<Integer, String> movieGenres,
                           Map<Integer, String> tvGenres) throws Exception {

        loadGenreCategory(apiUrl, token, "movie", movieGenres);
        loadGenreCategory(apiUrl, token, "tv", tvGenres);
    }

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