package models;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class containing helper methods
 * @author Mahmoud Saghir
 */
public class Utils {
    /**
     * Sends a HTTP GET request and returns the raw JSON response as JsonNode.
     *
     * @param urlStr API endpoint URL
     * @param token  Bearer token for authorization (can be null if not required)
     * @return JsonNode representing the JSON response from the API
     * @throws Exception if connection or reading stream fails
     * @author Mahmoud Saghir
     */
    public static JsonNode sendGetRequest(String urlStr, String token) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        conn.setRequestProperty("Accept", "application/json");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {

            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                builder.append(line);
            }

            return Json.parse(builder.toString());
        }
    }
}
