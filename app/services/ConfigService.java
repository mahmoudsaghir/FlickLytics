package services;

import com.fasterxml.jackson.databind.JsonNode;
import models.Utils;

/**
 * Service responsible for loading configuration constants.
 *
 * @author Mahmoud Saghir
 */
public class ConfigService {

    /**
     * Loads translation count constant from TMDb collection API.
     *
     * @param apiUrl TMDb base URL
     * @param token TMDb authentication token
     * @return translation list size constant
     */
    public int loadTargetLanguageConstant(String apiUrl, String token) {
        try {
            String urlStr = apiUrl + "collection/10/translations";

            JsonNode root = Utils.sendGetRequest(urlStr, token);
            JsonNode translations = root.path("translations");

            if (translations.isArray()) {
                return translations.size();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1; // fallback value
    }
}