package actors;

import com.fasterxml.jackson.databind.JsonNode;
import models.GlobalDiversityResult;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.GlobalDiversityService;
import services.TmdbService;

/**
 * Actor responsible for computing Global Diversity metrics.
 *
 * @author Mahmoud Saghir
 */
public class GlobalDiversityActor extends AbstractActor {

    private final GlobalDiversityService service;
    private final TmdbService tmdbService;
    private final String apiUrl;
    private final String tmdbToken;
    private final Logger logger = LoggerFactory.getLogger(GlobalDiversityActor.class);

    /**
     * Command to compute Global Diversity metrics.
     *
     * @author Mahmoud Saghir
     */
    public static class ComputeDiversity {
        public final String category;
        public final Long id;
        public final int targetLanguageConstant;

        /**
         * Constructor for ComputeDiversity.
         *
         * @param category               movie or tv
         * @param targetLanguageConstant normalization constant
         * @author Mahmoud Saghir
         */
        public ComputeDiversity(String category, Long id, int targetLanguageConstant) {
            this.category = category;
            this.id = id;
            this.targetLanguageConstant = targetLanguageConstant;
        }
    }

    /**
     * Constructor for GlobalDiversityActor.
     *
     * @param service     GlobalDiversityService instance
     * @param tmdbService TmdbService instance
     * @param apiUrl      API URL string
     * @param tmdbToken   TMDB token string
     * @author Mahmoud Saghir
     */
    public GlobalDiversityActor(GlobalDiversityService service, TmdbService tmdbService, String apiUrl, String tmdbToken) {
        this.service = service;
        this.tmdbService = tmdbService;
        this.apiUrl = apiUrl;
        this.tmdbToken = tmdbToken;
    }

    /**
     * Creates Props for GlobalDiversityActor.
     *
     * @param service GlobalDiversityService instance
     * @return Props for creating GlobalDiversityActor
     * @author Mahmoud Saghir
     */
    public static Props props(GlobalDiversityService service,
                              services.TmdbService tmdbService,
                              String apiUrl,
                              String tmdbToken) {
        return Props.create(GlobalDiversityActor.class,
                () -> new GlobalDiversityActor(service, tmdbService, apiUrl, tmdbToken));
    }

    /**
     * Creates receive builder for GlobalDiversityActor.
     *
     * @return Receive builder for GlobalDiversityActor
     * @author Mahmoud Saghir
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ComputeDiversity.class, this::onComputeDiversity)
                .matchAny(o -> logger.warn("Received unknown message: {}", o))
                .build();
    }

    /**
     * Handles ComputeDiversity message by calling GlobalDiversityService.compute().
     *
     * @param msg ComputeDiversity message
     * @author Mahmoud Saghir
     */
    private void onComputeDiversity(ComputeDiversity msg) throws Exception {
        logger.info("Received ComputeDiversity message: category={}, targetLanguageConstant={}", msg.category, msg.targetLanguageConstant);

        JsonNode detailsAndTranslationRoot = tmdbService.getDetailsAndTranslations(apiUrl, tmdbToken, msg.category, msg.id);
        GlobalDiversityResult result = service.compute(msg.category, detailsAndTranslationRoot, msg.targetLanguageConstant);

        logger.info("Computed GlobalDiversityResult: {}", result);
        getSender().tell(result, getSelf());
    }
}